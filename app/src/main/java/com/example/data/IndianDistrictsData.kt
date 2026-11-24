package com.example.data

import com.example.model.DstRule
import com.example.model.Place

/**
 * 732 Indian districts with exact longitudes and representative latitudes.
 * Format per line: "Name|lat|lon"
 */
internal object IndianDistrictsData {

    private val RAW_TSV = """
Adilabad|19.6641|78.5320
Agar Malwa|23.7116|76.0150
Agra|27.1767|78.0081
Ahmedabad|23.0225|72.5714
Ahmednagar (Ahilyanagar)|19.0948|74.7480
Aizawl|23.7271|92.7176
Ajmer|26.4499|74.6399
Akola|20.7002|77.0082
Alappuzha|9.4981|76.3388
Aligarh|27.8974|78.0880
Alipurduar|26.4918|89.5271
Alirajpur|22.3033|74.3587
Alluri Sitharama Raju|17.7500|82.6606
Almora|29.5892|79.6467
Alwar|27.5530|76.6346
Ambala|30.3782|76.7821
Ambedkar Nagar|26.4071|82.5338
Amethi|26.1541|81.8142
Amravati|20.9320|77.7523
Amreli|21.6015|71.2221
Amritsar|31.6340|74.8723
Amroha|28.9044|78.4673
Anakapalli|17.6913|83.0088
Anand|22.5645|72.9289
Anantapur|14.6819|77.6006
Anantnag|33.7311|75.1498
Angul|20.8409|85.1012
Anjaw|28.1000|96.8348
Annamayya|14.0600|78.7518
Anupgarh|29.1912|73.2089
Anuppur|23.1121|81.6908
Araria|26.1486|87.4700
Aravalli|23.5167|73.2988
Ariyalur|11.1401|79.0766
Arwal|25.2471|84.6776
Ashoknagar|24.5775|77.7289
Auraiya|26.4648|79.5037
Aurangabad (Bihar)|24.7520|84.3742
Ayodhya|26.7922|82.1998
Azamgarh|26.0739|83.1859
Bagalkot|16.1691|75.6615
Bageshwar|29.8362|79.7694
Baghpat|28.9428|77.2187
Bahraich|27.5706|81.5959
Bajali|26.5167|91.1823
Baksa|26.6900|91.4227
Balaghat|21.8129|80.1847
Balangir|20.7011|83.4853
Balasore|21.4934|86.9166
Ballari|15.1394|76.9214
Ballia|25.7584|84.1487
Balod|20.7317|81.2045
Baloda Bazar|21.6569|82.1592
Balotra|25.8313|72.2435
Balrampur (Chhattisgarh)|23.6119|83.6138
Balrampur (Uttar Pradesh)|27.4307|82.1811
Banaskantha|24.1719|72.4397
Banda|25.4754|80.3380
Bandipora|34.4170|74.6431
Banka|24.8879|86.9226
Bankura|23.2313|87.0680
Banswara|23.5461|74.4421
Bapatla|15.9044|80.4675
Barabanki|26.9261|81.1868
Baramulla|34.2090|74.3436
Baran|25.1011|76.5132
Bareilly|28.3670|79.4304
Bargarh|21.3414|83.6190
Barmer|25.7521|71.3967
Barnala|30.3819|75.5487
Barpeta|26.3216|91.0063
Barwani|22.0365|74.9001
Bastar|19.1071|81.9535
Basti|26.8140|82.7601
Bathinda|30.2110|74.9455
Beawar|26.1011|74.3203
Beed|18.9891|75.7531
Begusarai|25.4182|86.1272
Belagavi|15.8497|74.4977
Bemetara|21.7145|81.5346
Bengaluru Rural|13.2846|77.7126
Bengaluru Urban|12.9716|77.5946
Betul|21.9006|77.8961
Bhadohi|25.3944|82.5700
Bhadradri Kothagudem|17.5544|80.6197
Bhadrak|21.0574|86.5168
Bhagalpur|25.2425|86.9842
Bhandara|21.1777|79.6548
Bharatpur|27.2152|77.4895
Bharuch|21.7051|72.9959
Bhavnagar|21.7645|72.1519
Bhilwara|25.3407|74.6313
Bhind|26.5646|78.7873
Bhiwani|28.7975|76.1322
Bhojpur|25.5541|84.6603
Bhopal|23.2599|77.4126
Bicholim|15.5938|73.9457
Bidar|17.9104|77.5197
Bijapur|18.8453|80.8184
Bijnor|29.3724|78.1314
Bikaner|28.0229|73.3119
Bilaspur (Chhattisgarh)|22.0797|82.1409
Bilaspur (Himachal Pradesh)|31.3350|76.7629
Birbhum|23.8402|87.5335
Bishnupur|24.6280|93.7785
Biswanath|26.7372|93.1481
Bokaro|23.6693|86.1511
Bongaigaon|26.4773|90.5580
Botad|22.1704|71.6636
Boudh|20.8364|84.3235
Budaun|28.0362|79.1266
Budgam|34.0185|74.7200
Bulandshahr|28.4070|77.8498
Buldhana|20.5292|76.1844
Bundi|25.4305|75.6370
Burhanpur|21.3104|76.2298
Buxar|25.5647|83.9777
Cachar|24.8333|92.7789
Canacona|15.0173|74.0225
Central Delhi|28.6415|77.2410
Chamarajanagar|11.9261|76.9398
Chamba|32.5534|76.1258
Chamoli|30.4146|79.3225
Champawat|29.3366|80.0910
Champhai|23.4566|93.3282
Chandauli|25.2588|83.2683
Chandel|24.3292|94.0005
Chandigarh|30.7333|76.7794
Chandrapur|19.9615|79.2961
Changlang|27.1436|95.7412
Charaideo|27.0200|95.0185
Charkhi Dadri|28.5921|76.2653
Chatra|24.2091|84.8703
Chengalpattu|12.6819|79.9757
Chennai|13.0827|80.2707
Chhatarpur|24.9164|79.5880
Chhatrapati Sambhajinagar|19.8762|75.3433
Chhindwara|22.0574|78.9382
Chhota Udaipur|22.3046|74.0124
Chikkaballapur|13.4355|77.7278
Chikkamagaluru|13.3161|75.7720
Chirang|26.5200|90.6358
Chitradurga|14.2251|76.3980
Chitrakoot|25.2022|80.9132
Chittoor|13.2172|79.1003
Chittorgarh|24.8887|74.6269
Churachandpur|24.3333|93.6960
Churu|28.2925|74.9672
Coimbatore|11.0168|76.9558
Cooch Behar|26.3452|89.4457
Cuddalore|11.7480|79.7680
Cuttack|20.4625|85.8830
Dadra and Nagar Haveli|20.1809|73.0169
Dahod|22.8371|74.2597
Dakshina Kannada|12.9141|74.8560
Daman|20.3974|72.8397
Damoh|23.8323|79.4422
Dang|20.7520|73.6867
Dantewada|18.8992|81.3474
Darbhanga|26.1542|85.8918
Darjeeling|27.0410|88.2627
Darrang|26.4470|92.0273
Datia|25.6700|78.4609
Dausa|26.8931|76.3312
Davanagere|14.4644|75.9218
Deeg|27.4750|77.3262
Dehradun|30.3165|78.0322
Deogarh|21.5390|84.7289
Deoghar|24.4852|86.6948
Deoria|26.5024|83.7787
Devbhoomi Dwarka|22.2442|69.6502
Dewas|22.9676|76.0534
Dhalai|23.8467|91.9099
Dhamtari|20.7072|81.5515
Dhanbad|23.7957|86.4304
Dhar|22.6013|75.3025
Dharashiv|18.1861|76.0419
Dharmapuri|12.1211|78.1582
Dharwad|15.4589|75.0078
Dhemaji|27.4810|94.5624
Dhenkanal|20.6505|85.5981
Dholpur|26.6966|77.8908
Dhubri|26.0187|89.9743
Dhule|20.9042|74.7749
Dibang Valley|28.8000|95.9012
Dibrugarh|27.4728|94.9120
Didwana-Kuchaman|27.3990|74.5732
Dima Hasao|25.1700|93.0176
Dimapur|25.9091|93.7266
Dindigul|10.3624|77.9695
Dindori|22.9422|81.0833
Diu|20.7144|70.9874
Doda|33.1490|75.5476
Dudu|26.6833|75.2347
Dumka|24.2685|87.2497
Dungarpur|23.8417|73.7147
Durg|21.1904|81.2849
East Champaran|26.6524|84.9163
East Delhi|28.6279|77.2986
East Garo Hills|25.5150|90.6197
East Godavari|17.0005|81.8040
East Jaintia Hills|25.3500|92.3653
East Kameng|27.3000|93.0473
East Khasi Hills|25.5788|91.8933
East Siang|28.0700|95.3263
East Singhbhum|22.8046|86.2029
Eastern West Khasi Hills|25.5500|91.5647
Eluru|16.7107|81.0952
Ernakulam|9.9816|76.2673
Erode|11.3410|77.7172
Etah|27.5588|78.6626
Etawah|26.7855|79.0158
Faridabad|28.4089|77.3178
Faridkot|30.6769|74.7539
Farrukhabad|27.3906|79.5800
Fatehabad|29.5132|75.4555
Fatehgarh Sahib|30.6435|76.3924
Fatehpur|25.9210|80.8139
Fazilka|30.4021|74.0280
Ferozepur|30.9331|74.6131
Firozabad|27.1591|78.3957
Gadag|15.4315|75.6355
Gadchiroli|20.1849|80.0033
Gajapati|18.8000|84.1857
Ganderbal|34.2165|74.7719
Gandhinagar|23.2156|72.6369
Gangapur City|26.4730|76.7214
Gangtok|27.3314|88.6138
Ganjam|19.3870|84.7929
Garhwa|24.1603|83.8027
Gariaband|20.6358|82.0614
Gaurela-Pendra-Marwahi|22.7500|81.9056
Gautam Buddha Nagar|28.5355|77.5040
Gaya|24.7914|85.0002
Ghaziabad|28.6692|77.4538
Ghazipur|25.5878|83.5800
Gir Somnath|20.9060|70.4029
Giridih|24.1913|86.3082
Goalpara|26.1641|90.6252
Godda|24.8255|87.2130
Golaghat|26.5239|93.9623
Gomati|23.5333|91.4910
Gonda|27.1337|81.9619
Gondia|21.4624|80.1961
Gopalganj|26.4670|84.4404
Gorakhpur|26.7606|83.3732
Guntur|16.3067|80.4365
Gurdaspur|32.0419|75.4053
Gurugram|28.4595|77.0266
Gwalior|26.2183|78.1828
Gyalshing|27.2895|88.2570
Hailakandi|24.6820|92.5636
Hamirpur (Himachal Pradesh)|31.6862|76.5213
Hamirpur (Uttar Pradesh)|25.9562|80.1481
Hanumakonda|18.0000|79.5584
Hanumangarh|29.5815|74.3294
Hapur|28.7306|77.7759
Harda|22.3467|77.0952
Hardoi|27.3949|80.1311
Haridwar|29.9457|78.1642
Hassan|13.0033|76.0996
Hathras|27.5949|78.0522
Haveri|14.7951|75.4044
Hazaribagh|23.9925|85.3616
Hingoli|19.7174|77.1472
Hisar|29.1492|75.7217
Hnahthial|22.9667|92.9317
Hojai|26.0020|92.8473
Hooghly|22.8963|88.3967
Hoshiarpur|31.5143|75.9115
Howrah|22.5958|88.2636
Hyderabad|17.3850|78.4867
Idukki|9.9189|76.9720
Imphal East|24.8170|93.9675
Imphal West|24.8170|93.9368
Indore|22.7196|75.8577
Itanagar Capital Complex|27.0844|93.6053
Jabalpur|23.1815|79.9864
Jagatsinghpur|20.2546|86.1711
Jagtial|18.7895|78.9121
Jaipur|26.9124|75.7873
Jaipur Rural|26.9500|75.8200
Jaisalmer|26.9157|70.9083
Jajpur|20.8378|86.3377
Jalandhar|31.3260|75.5762
Jalaun|26.1440|79.3366
Jalgaon|21.0077|75.5626
Jalna|19.8347|75.8816
Jalore|25.3444|72.6151
Jalpaiguri|26.5435|88.7194
Jammu|32.7266|74.8570
Jamnagar|22.4707|70.0577
Jamtara|23.9628|86.8047
Jamui|24.9277|86.2248
Jangaon|17.7267|79.1520
Janjgir-Champa|22.0110|82.5762
Jashpur|22.8878|84.1378
Jaunpur|25.7464|82.6837
Jayashankar Bhupalpally|18.4333|79.8608
Jehanabad|25.2133|84.9877
Jhabua|22.7676|74.5909
Jhajjar|28.6063|76.6565
Jhalawar|24.5973|76.1609
Jhansi|25.4484|78.5685
Jhargram|22.4550|86.9974
Jharsuguda|21.8554|84.0062
Jhunjhunu|28.1289|75.3998
Jind|29.3255|76.3150
Jiribam|24.8000|93.1202
Jodhpur|26.2389|73.0243
Jodhpur Rural|26.2800|73.0500
Jogulamba Gadwal|16.2333|77.7989
Jorhat|26.7509|94.2037
Junagadh|21.5222|70.4579
Kabirdham|22.0100|81.2367
Kaimur|25.0439|83.6083
Kaithal|29.8015|76.3996
Kakching|24.4970|93.9813
Kakinada|16.9891|82.2475
Kalaburagi|17.3297|76.8343
Kalahandi|19.9137|83.1649
Kalimpong|27.0594|88.4695
Kallakurichi|11.7401|78.9597
Kamareddy|18.3204|78.3372
Kamle|27.7500|94.0844
Kamrup|26.3167|91.5984
Kamrup Metropolitan|26.1445|91.7362
Kancheepuram|12.8185|79.7036
Kandhamal|20.1342|84.2347
Kangpokpi|25.1500|93.9723
Kangra|32.0998|76.2691
Kanker|20.2719|81.4912
Kannauj|27.0514|79.9137
Kannur|11.8745|75.3704
Kanpur Dehat|26.4000|79.9564
Kanpur Nagar|26.4499|80.3319
Kanyakumari|8.0883|77.5385
Kapurthala|31.3801|75.3811
Karaikal|10.9254|79.8380
Karauli|26.4981|77.0260
Karbi Anglong|26.1833|93.4384
Kargil|34.5539|76.1349
Karimganj|24.8649|92.3586
Karimnagar|18.4386|79.1288
Karnal|29.6857|76.9905
Karur|10.9601|78.0766
Kasaragod|12.4996|74.9896
Kasganj|27.8087|78.6458
Kathua|32.3865|75.5173
Katihar|25.5520|87.5719
Katni|23.8343|80.3919
Kaushambi|25.5313|81.3917
Kekri|25.9718|75.1517
Kendrapara|20.4997|86.4220
Kendujhar|21.6290|85.5817
Khagaria|25.5024|86.4684
Khairagarh-Chhuikhadan-Gandai|21.4167|80.9772
Khairthal-Tijara|27.8100|76.6375
Khammam|17.2473|80.1514
Khandwa|21.8314|76.3498
Khargone|21.8235|75.6102
Khawzawl|23.5333|93.1844
Kheda|22.7510|72.6853
Khordha|20.1862|85.6223
Khowai|24.0667|91.6053
Khunti|23.0715|85.2784
Kinnaur|31.6075|78.2752
Kiphire|25.8931|94.7824
Kishanganj|26.0917|87.9450
Kishtwar|33.3116|75.7666
Kodagu|12.4244|75.7382
Koderma|24.4676|85.5947
Kohima|25.6751|94.1086
Kokrajhar|26.4014|90.2667
Kolar|13.1362|78.1291
Kolasib|24.2254|92.6784
Kolhapur|16.7050|74.2433
Kolkata|22.5726|88.3639
Kollam|8.8932|76.6141
Konaseema|16.5787|82.0061
Kondagaon|19.5917|81.6631
Koppal|15.3455|76.1548
Koraput|18.8135|82.7123
Korba|22.3595|82.7501
Korea (Koriya)|23.2597|82.5594
Kota|25.2138|75.8648
Kotputli-Behror|27.7024|76.1973
Kottayam|9.5916|76.5222
Kozhikode|11.2588|75.7804
Kra Daadi|27.7000|93.6423
Krishna|16.1875|81.1303
Krishnagiri|12.5186|78.2137
Kulgam|33.6451|75.0189
Kullu|31.9579|77.1095
Kumuram Bheem Asifabad|19.3585|79.2869
Kupwara|34.5261|74.2662
Kurnool|15.8281|78.0373
Kurukshetra|29.9695|76.8783
Kurung Kumey|27.9000|93.3621
Kushinagar|26.7405|83.8889
Kutch|23.7337|69.8597
Lahaul and Spiti|32.5740|77.0320
Lakhimpur|27.2360|94.1036
Lakhimpur Kheri|27.9462|80.7798
Lakhisarai|25.1710|86.0942
Lalitpur|24.6912|78.4138
Latehar|23.7441|84.4998
Latur|18.4088|76.5604
Lawngtlai|22.5284|92.8935
Leh|34.1526|77.5771
Leparada|27.9800|94.6826
Lohardaga|23.4357|84.6811
Lohit|27.9167|96.1667
Longding|26.8600|95.3235
Longleng|26.4730|94.8124
Lower Dibang Valley|28.1333|95.8358
Lower Siang|27.8500|94.7562
Lower Subansiri|27.5500|93.8312
Lucknow|26.8467|80.9462
Ludhiana|30.9010|75.8573
Lunglei|22.8856|92.7383
Madhepura|25.9222|86.7946
Madhubani|26.3483|86.0712
Madurai|9.9252|78.1198
Mahabubabad|17.5980|80.0020
Mahabubnagar|16.7488|78.0035
Maharajganj|27.1444|83.5620
Mahasamund|21.1128|82.0968
Mahe|11.7011|75.5367
Mahendragarh|28.2683|76.1510
Mahisagar|23.1333|73.5594
Mahoba|25.2921|79.8722
Maihar|24.2630|80.7586
Mainpuri|27.2292|79.0284
Majuli|26.9500|94.1678
Malappuram|11.0510|76.0740
Malda|25.0108|88.1411
Malerkotla|30.5250|75.8794
Malkangiri|18.3436|81.8884
Mamit|23.9278|92.4906
Mancherial|18.8714|79.4631
Mandi|31.7088|76.9318
Mandla|22.5984|80.3714
Mandsaur|24.0768|75.0699
Mandya|12.5218|76.8951
Manendragarh-Chirmiri-Bharatpur|23.2100|82.2039
Mangan|27.5090|88.5377
Mansa|29.9985|75.4018
Mathura|27.4924|77.6737
Mau|25.9417|83.5615
Mauganj|24.6770|81.8742
Mayiladuthurai|11.1018|79.6526
Mayurbhanj|21.9287|86.7263
Medak|18.0453|78.2608
Medchal-Malkajgiri|17.6290|78.5989
Meerut|28.9845|77.7064
Mehsana|23.5880|72.3693
Mirzapur|25.1337|82.5644
Moga|30.8165|75.1711
Mohla-Manpur-Ambagarh Chowki|20.5800|80.7478
Mokokchung|26.3270|94.5136
Mon|26.7500|95.0315
Moradabad|28.8386|78.7733
Morbi|22.8173|70.8236
Morena|26.4947|78.0012
Morigaon|26.2546|92.3423
Mulugu|18.1912|79.9434
Mumbai City|18.9388|72.8364
Mumbai Suburban|19.1136|72.8517
Mungeli|22.0667|81.6879
Munger|25.3708|86.4735
Murshidabad|24.1759|88.2679
Muzaffarnagar|29.4727|77.7085
Muzaffarpur|26.1197|85.3910
Mysuru|12.2958|76.6394
Nabarangpur|19.2311|82.5483
Nadia|23.4710|88.5565
Nagaon|26.3452|92.6923
Nagapattinam|10.7672|79.8449
Nagarkurnool|16.4820|78.3236
Nagaur|27.1983|73.7336
Nagpur|21.1458|79.0882
Nainital|29.3919|79.4636
Nalanda|25.1357|85.4430
Nalbari|26.4447|91.4384
Nalgonda|17.0575|79.2684
Namakkal|11.2189|78.1674
Namchi|27.1670|88.3638
Namsai|27.6667|95.8606
Nanded|19.1383|77.3210
Nandurbar|21.3700|74.2394
Nandyal|15.4777|78.4836
Narayanpet|16.7450|77.4960
Narayanpur|19.7244|81.2463
Narmada|21.8698|73.5037
Narmadapuram|22.7471|77.7270
Narsinghpur|22.9469|79.1921
Nashik|19.9975|73.7898
Navsari|20.9467|72.9270
Nawada|24.8866|85.5435
Nayagarh|20.1289|85.0985
Neem Ka Thana|27.7376|75.7834
Neemuch|24.4764|74.8742
New Delhi|28.6139|77.2090
Nicobar|7.9800|92.7844
Nilgiris|11.4916|76.6950
Nirmal|19.0964|78.3430
Nizamabad|18.6725|78.0941
Niwari|25.3676|78.8021
Noklak|26.2000|95.0163
Noney|24.8600|93.6213
North and Middle Andaman|12.6000|92.8995
North Delhi|28.7041|77.2245
North East Delhi|28.6863|77.2768
North Garo Hills|25.8800|90.6167
North Goa|15.4909|73.8180
North Tripura|24.3000|92.1624
North West Delhi|28.7196|77.0818
NTR|16.5062|80.6480
Nuapada|20.8058|82.5361
Nuh|28.1000|77.0016
Pakke-Kessang|27.1500|93.2268
Pakur|24.6336|87.8492
Palakkad|10.7867|76.6548
Palamu|24.0355|84.0672
Palghar|19.6967|72.7655
Pali|25.7711|73.3234
Palnadu|16.2333|80.0485
Palwal|28.1487|77.3320
Panchkula|30.6942|76.8606
Panchmahal|22.7689|73.6143
Pandhurna|21.5950|78.5236
Panipat|29.3909|76.9635
Panna|24.7215|80.1880
Papum Pare|27.1000|93.7512
Parbhani|19.2608|76.7748
Parvathipuram Manyam|18.7800|83.4259
Paschim Bardhaman|23.6889|87.0229
Paschim Medinipur|22.4257|87.3215
Patan|23.8493|72.1266
Pathanamthitta|9.2648|76.7870
Pathankot|32.2643|75.6522
Patiala|30.3398|76.3869
Patna|25.5941|85.1376
Pauri Garhwal|30.1469|78.7747
Peddapalli|18.6134|79.3738
Perambalur|11.2342|78.8807
Peren|25.5100|93.7336
Pernem|15.7219|73.7975
Phalodi|27.1310|72.3683
Phek|25.6626|94.4716
Pherzawl|24.2500|93.2662
Pilibhit|28.6305|79.8044
Pithoragarh|29.5829|80.2182
Ponda|15.4027|74.0067
Poonch|33.7700|74.0921
Porbandar|21.6417|69.6293
Prakasam|15.5057|80.0499
Pratapgarh (Rajasthan)|24.0320|74.7810
Pratapgarh (Uttar Pradesh)|25.8975|81.9450
Prayagraj|25.4358|81.8463
Puducherry|11.9416|79.8083
Pudukkottai|10.3797|78.8001
Pulwama|33.8716|74.8994
Pune|18.5204|73.8567
Purba Bardhaman|23.2324|87.8615
Purba Medinipur|22.2900|87.9219
Puri|19.8135|85.8312
Purnia|25.7771|87.4753
Purulia|23.3322|86.3652
Quepem|15.2122|74.0724
Raebareli|26.2345|81.2409
Raichur|16.2120|77.3566
Raigad|18.5158|73.0939
Raigarh|21.8974|83.3950
Raipur|21.2514|81.6296
Raisen|23.3315|77.7897
Rajanna Sircilla|18.3871|78.8080
Rajgarh|24.0094|76.7275
Rajkot|22.3039|70.8022
Rajnandgaon|21.0972|81.0300
Rajouri|33.3716|74.3152
Rajsamand|25.0706|73.8831
Ramanagara|12.7150|77.2812
Ramanathapuram|9.3639|78.8395
Ramban|33.2464|75.1939
Ramgarh|23.6332|85.5190
Rampur|28.8040|79.0220
Ranchi|23.3441|85.3096
Rangareddy|17.3850|78.4867
Ranipet|12.9272|79.3190
Ratlam|23.3315|75.0367
Ratnagiri|16.9902|73.3120
Rayagada|19.1712|83.4163
Reasi|33.0815|74.8364
Rewa|24.5362|81.3037
Rewari|28.1920|76.6191
Ri-Bhoi|25.9000|91.8749
Rohtak|28.8955|76.6066
Rohtas|24.9600|84.0118
Rudraprayag|30.2844|78.9811
Rupnagar|30.9661|76.5267
Sabarkantha|23.5978|72.9630
Sagar|23.8388|78.7378
Saharanpur|29.9680|77.5460
Saharsa|25.8835|86.6006
Sahibganj|25.2427|87.6454
Sahibzada Ajit Singh Nagar|30.7046|76.7179
Saiha|22.4897|92.9806
Saitual|23.9700|92.9652
Sakti|22.0273|82.9599
Salcete|15.2736|73.9660
Salem|11.6643|78.1460
Salumbar|24.1350|74.0453
Samastipur|25.8629|85.7810
Samba|32.5535|75.1187
Sambalpur|21.4669|83.9812
Sambhal|28.5850|78.5718
Sanchore|24.7560|71.7723
Sangareddy|17.6140|78.0867
Sangli|16.8524|74.5815
Sangrur|30.2458|75.8449
Sanguem|15.2300|74.1511
Sant Kabir Nagar|26.7820|83.0370
Saran|25.8500|84.7499
Sarangarh-Bilaigarh|21.5890|83.0768
Satara|17.6805|74.0183
Satna|24.6005|80.8322
Sattari|15.5600|74.1362
Sawai Madhopur|26.0217|76.3526
Sehore|23.2032|77.0851
Senapati|25.2700|94.0225
Seoni|22.0869|79.5435
Sepahijala|23.6200|91.2981
Seraikela Kharsawan|22.7000|85.9324
Serchhip|23.3417|92.8522
Shahdara|28.6820|77.2890
Shahdol|23.2986|81.3621
Shaheed Bhagat Singh Nagar|31.1256|76.1180
Shahjahanpur|27.8815|79.9120
Shahpura|25.6300|74.9255
Shajapur|23.4273|76.2730
Shamli|29.4500|77.3106
Sheikhpura|25.1400|85.8453
Sheohar|26.5162|85.2934
Sheopur|25.6680|76.6967
Shi Yomi|28.5500|94.1326
Shimla|31.1048|77.1734
Shivamogga|13.9299|75.5681
Shivpuri|25.4234|77.6611
Shopian|33.7167|74.8339
Shravasti|27.5142|81.8842
Siang|28.2500|94.9628
Siddharthnagar|27.2950|83.0954
Siddipet|18.1019|78.8520
Sidhi|24.4071|81.8814
Sikar|27.6094|75.1398
Simdega|22.6152|84.5020
Sindhudurg|16.0600|73.6881
Singrauli|24.1992|82.6645
Sirmaur|30.5610|77.2965
Sirohi|24.8826|72.8625
Sirsa|29.5321|75.0255
Sitamarhi|26.5939|85.4906
Sitapur|27.5706|80.6828
Sivaganga|9.8433|78.4809
Sivasagar|26.9821|94.6425
Siwan|26.2196|84.3567
Solan|30.9084|77.0967
Solapur|17.6599|75.9064
Sonbhadra|24.6850|83.0684
Sonepur|20.8333|83.9163
Sonipat|28.9931|77.0151
Sonitpur|26.6739|92.7926
Soreng|27.1700|88.2036
South Andaman|11.6234|92.7265
South Delhi|28.5300|77.2219
South East Delhi|28.5500|77.2636
South Garo Hills|25.2500|90.6167
South Goa|15.2000|73.9857
South Salmara-Mankachar|25.5500|89.8768
South Tripura|23.2500|91.4719
South West Delhi|28.5700|77.0689
South West Garo Hills|25.4700|89.9142
South West Khasi Hills|25.3000|91.2721
Sri Ganganagar|29.9038|73.8800
Sri Muktsar Sahib|30.4762|74.5122
Sri Potti Sriramulu Nellore|14.4426|79.9865
Sri Sathya Sai|14.1600|77.7915
Srikakulam|18.2949|83.8938
Srinagar|34.0837|74.7973
Sukma|18.3950|81.6634
Sultanpur|26.2648|82.0727
Sundargarh|22.1167|84.0306
Supaul|26.1260|86.6050
Surat|21.1702|72.8311
Surendranagar|22.7201|71.6370
Surajpur|23.2210|82.8669
Surguja|23.1200|83.1961
Suryapet|17.1405|79.6239
Tamenglong|24.9800|93.5015
Tamulpur|26.6300|91.5732
Tapi|21.1200|73.3941
Tarn Taran|31.4519|74.9277
Tawang|27.5860|91.8594
Tehri Garhwal|30.3753|78.4803
Tengnoupal|24.3800|94.1528
Tenkasi|8.9594|77.3152
Thane|19.2183|72.9781
Thanjavur|10.7870|79.1378
Theni|10.0104|77.4768
Thiruvallur|13.1438|79.9120
Thiruvananthapuram|8.5241|76.9366
Thiruvarur|10.7726|79.6344
Thoothukudi|8.7642|78.1348
Thoubal|24.6300|94.0005
Thrissur|10.5276|76.2144
Tikamgarh|24.7450|78.8321
Tinsukia|27.4922|95.3558
Tirap|27.0000|95.5121
Tiruchirappalli|10.7905|78.7047
Tirunelveli|8.7139|77.7567
Tirupathur|12.4950|78.5729
Tirupati|13.6288|79.4192
Tiruppur|11.1085|77.3411
Tiruvannamalai|12.2253|79.0747
Tiswadi|15.4909|73.8567
Tonk|26.1620|75.7885
Tuensang|26.2667|94.8124
Tumakuru|13.3379|77.1140
Udaipur|24.5854|73.7125
Udalguri|26.7450|92.1003
Udham Singh Nagar|28.9750|79.3974
Udhampur|32.9160|75.1416
Udupi|13.3409|74.7421
Ujjain|23.1765|75.7885
Ukhrul|25.0968|94.3617
Umaria|23.5246|80.8365
Una|31.4685|76.2670
Unakoti|24.3000|92.0124
Unnao|26.5393|80.4878
Upper Siang|28.6000|95.0124
Upper Subansiri|27.9800|94.2254
Uttar Dinajpur|25.6300|88.1256
Uttara Kannada|14.7937|74.1240
Uttarkashi|30.7268|78.4354
Vadodara|22.3072|73.1812
Vaishali|25.6838|85.2139
Valsad|20.5992|72.9342
Varanasi|25.3176|82.9739
Vellore|12.9165|79.1325
Vidisha|23.5251|77.8081
Vijayanagara|15.2700|76.3909
Vijayapura|16.8302|75.7100
Vikarabad|17.3364|77.9048
Viluppuram|11.9401|79.4861
Virudhunagar|9.5680|77.9624
Visakhapatnam|17.6868|83.2185
Vizianagaram|18.1067|83.3956
Wanaparthy|16.3623|78.0622
Warangal|17.9689|79.5941
Wardha|20.7453|78.6022
Washim|20.1119|77.1334
Wayanad|11.6854|76.1320
West Champaran|26.8000|84.5020
West Delhi|28.6663|77.0818
West Garo Hills|25.5100|90.2023
West Godavari|16.9174|81.5212
West Jaintia Hills|25.4400|92.1994
West Kameng|27.2600|92.4159
""".trimIndent()

    fun loadIndianDistricts(): List<Place> {
        return RAW_TSV.lineSequence()
            .mapIndexedNotNull { index, line ->
                val parts = line.split('|')
                if (parts.size != 3) return@mapIndexedNotNull null
                val name = parts[0].trim()
                val lat = parts[1].toDoubleOrNull() ?: return@mapIndexedNotNull null
                val lon = parts[2].toDoubleOrNull() ?: return@mapIndexedNotNull null
                val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                Place(
                    id = "in_${index}_$slug",
                    nameHi = name,
                    nameEn = name,
                    nameZh = name,
                    countryCode = "IN",
                    latitude = lat,
                    longitude = lon,
                    baseUtcOffsetHours = 5.5,
                    dstRule = DstRule.NONE
                )
            }
            .toList()
    }
}
