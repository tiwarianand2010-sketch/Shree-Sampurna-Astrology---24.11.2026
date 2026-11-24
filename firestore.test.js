const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validKundaliPayload(uid, override = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    chartName: "India 1947 Chart",
    birthYear: 1947,
    birthMonth: 8,
    birthDay: 15,
    birthHour: 0,
    birthMinute: 0,
    placeId: "in_delhi",
    placeName: "New Delhi",
    latitude: 28.6139,
    longitude: 77.2090,
    utcOffsetHours: 5.5,
    lagnaRashi: "VRISHABHA",
    moonRashi: "KARKA",
    notes: "Independence Day Horoscope",
    createdAt: now,
    updatedAt: now,
    ...override,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write saved kundalis", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/kundalis`).get());
  await assertFails(
    unauthDb
      .collection(`users/${ALICE_UID}/kundalis`)
      .doc("chart_1")
      .set(validKundaliPayload(ALICE_UID))
  );
});

test("Authenticated owner can create, list, update, and delete valid kundali", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection(`users/${ALICE_UID}/kundalis`);

  // Create
  await assertSucceeds(col.doc("chart_1").set(validKundaliPayload(ALICE_UID)));

  // List query matching Kotlin CloudChartsRepository
  await assertSucceeds(col.orderBy("updatedAt", "desc").limit(50).get());

  // Partial update preserving immutable userId and createdAt
  await assertSucceeds(
    col.doc("chart_1").update({
      chartName: "Updated Independence Chart",
      notes: "Reviewed Ashtakavarga",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    col.doc("chart_1").update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Delete own chart
  await assertSucceeds(col.doc("chart_1").delete());
});

test("Reject shadow fields, @DocumentId 'id' leakage, out-of-range values, and future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection(`users/${ALICE_UID}/kundalis`);

  // Shadow/ghost field
  await assertFails(
    col.doc("chart_ghost").set(validKundaliPayload(ALICE_UID, { isAdmin: true }))
  );

  // @DocumentId 'id' field included in payload
  await assertFails(
    col.doc("chart_id").set(validKundaliPayload(ALICE_UID, { id: "chart_id" }))
  );

  // Out of bounds year
  await assertFails(
    col.doc("chart_year").set(validKundaliPayload(ALICE_UID, { birthYear: 1500 }))
  );

  // Tampered future timestamp
  await assertFails(
    col.doc("chart_future").set(
      validKundaliPayload(ALICE_UID, {
        createdAt: new Date(Date.now() + 120000),
        updatedAt: new Date(Date.now() + 120000),
      })
    )
  );
});

test("Cross-user access and spoofing are denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection(`users/${BOB_UID}/kundalis`)
      .doc("bob_chart")
      .set(validKundaliPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection(`users/${BOB_UID}/kundalis`).get());
  await assertFails(
    aliceDb.collection(`users/${BOB_UID}/kundalis`).doc("bob_chart").get()
  );
  await assertFails(
    aliceDb
      .collection(`users/${BOB_UID}/kundalis`)
      .doc("bob_chart")
      .update({ chartName: "Hijacked" })
  );
  await assertFails(
    aliceDb
      .collection(`users/${ALICE_UID}/kundalis`)
      .doc("spoof_chart")
      .set(validKundaliPayload(BOB_UID))
  );
});
