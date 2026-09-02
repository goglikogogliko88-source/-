package com.example.data.local

import com.example.data.model.*

object InitialData {

    val sampleSpecies = listOf(
        // 1. მწყერი
        SpeciesEntity(
            id = 1,
            nameGeo = "მწყერი",
            scientificName = "Coturnix coturnix",
            category = "გადამფრენი ფრინველი",
            isSeasonOpen = true,
            seasonDates = "აგვისტოს მე-3 შაბათი – 15 თებერვალი",
            dailyLimit = 20,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "ელექტრონული ხმის გამომცემი მოწყობილობები (მანოკები)",
                "ღამის ხედვის ოპტიკა და თერმოვიზორი",
                "სატრანსპორტო საშუალებიდან სროლა"
            ),
            description = "მცირე ზომის გადამფრენი ფრინველი ხოხბისებრთა ოჯახიდან. საქართველოში ერთ-ერთი ყველაზე პოპულარული სანადირო ობიექტია.",
            habitat = "მარცვლეულის ნათესები, იონჯის ველები, ალპური და სუბალპური მდელოები.",
            huntingTips = "ნადირობა ძირითადად ხდება მეძებარი ძაღლით (პოინტერი, სეტერი, კურცხაარი). საუკეთესო დროა დილის და საღამოს საათები.",
            identification = "სიგრძე 16-18 სმ, ჭრელი მოყავისფრო-ქვიშისფერი შეფერილობა, სწრაფი დაბალი ფრენა.",
            legalStatus = "სავალდებულოა სახელმწიფო მოსაკრებლის (10 ლარი) გადახდის ქვითარი. დღიური ლიმიტი: 20 ცალი.",
            isProtected = false
        ),
        // 2. ქედანი
        SpeciesEntity(
            id = 2,
            nameGeo = "ქედანი",
            scientificName = "Columba palumbus",
            category = "გადამფრენი ფრინველი",
            isSeasonOpen = true,
            seasonDates = "აგვისტოს მე-3 შაბათი – 15 თებერვალი",
            dailyLimit = 10,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "მანქანიდან ან ძრავიანი საშუალებებიდან ნადირობა",
                "ელექტრონული მანოკები"
            ),
            description = "დიდი ზომის ველური მტრედი. ხასიათდება სწრაფი და მანევრირებადი ფრენით.",
            habitat = "ფოთლოვანი და შერეული ტყეები, ტყისპირები, მზესუმზირის და სიმინდის ყანები.",
            huntingTips = "ჩასაფრებით ნადირობა საკვებ ადგილებთან ან დასაჯდომ ხეებთან. რეკომენდებულია პროფილების (ფიტულების) გამოყენება.",
            identification = "მტრედისებრთაგან ყველაზე დიდი, კისერზე თეთრი ლაქები და ფრთებზე თეთრი ზოლები.",
            legalStatus = "დღიური ლიმიტი: 10 ცალი ერთ მონადირეზე.",
            isProtected = false
        ),
        // 3. გარეული იხვი
        SpeciesEntity(
            id = 3,
            nameGeo = "გარეული იხვი",
            scientificName = "Anas platyrhynchos",
            category = "წყალმცურავი ფრინველი",
            isSeasonOpen = false,
            seasonDates = "1 ნოემბერი – 1 მარტი",
            dailyLimit = 6,
            status = "დაშვებულია (სეზონზე)",
            prohibitedMethods = listOf(
                "ტყვიის საფანტის გამოყენება ჭარბტენიან ზონებში",
                "ელექტრონული მანოკი",
                "ძრავიანი ნავით დევნა"
            ),
            description = "საქართველოს ჭარბტენიან ზონებში ფართოდ გავრცელებული წყალმცურავი ფრინველი.",
            habitat = "ტბები, ჭაობები, მდინარეების დელტები (კოლხეთის დაბლობი, ჯავახეთის ტბები).",
            huntingTips = "დილის და საღამოს გადაფრენებზე ჩასაფრება, მანოკებისა და მცურავი ფიტულების გამოყენება.",
            identification = "მამალს აქვს მწვანე მბზინავი თავი, ყვითელი ნისკარტი და თეთრი საყელო.",
            legalStatus = "დღიური ლიმიტი: 6 ცალი. ნადირობა ნებადართულია მხოლოდ სეზონის პერიოდში.",
            isProtected = false
        ),
        // 4. გვრიტი
        SpeciesEntity(
            id = 4,
            nameGeo = "გვრიტი",
            scientificName = "Streptopelia turtur",
            category = "გადამფრენი ფრინველი",
            isSeasonOpen = true,
            seasonDates = "აგვისტოს მე-3 შაბათი – 15 თებერვალი",
            dailyLimit = 5,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "ბადეებით ან ხაფანგებით დაჭერა",
                "ელექტრონული ხმის იმიტატორი"
            ),
            description = "მცირე ზომის მოხდენილი მტრედისებრი. ხასიათდება სწრაფი, ტალღისებური ფრენით.",
            habitat = "ჭალის ტყეები, ბაღები, ტყისპირები, მინდვრების მიმდებარე ტყე-ზოლები.",
            huntingTips = "ჩასაფრება წყალსასმელებთან და მზესუმზირის ყანების პირას.",
            identification = "მოწითალო-ქვიშისფერი შეფერილობა, კისრის გვერდებზე შავ-თეთრი ზოლები.",
            legalStatus = "დღიური ლიმიტი: 5 ცალი. გადამფრენ ფრინველთა ოფიციალური ნუსხა.",
            isProtected = false
        ),
        // 5. ტყის ქათამი (ვალდშნეპი)
        SpeciesEntity(
            id = 5,
            nameGeo = "ტყის ქათამი (ვალდშნეპი)",
            scientificName = "Scolopax rusticola",
            category = "გადამფრენი ფრინველი",
            isSeasonOpen = false,
            seasonDates = "1 ოქტომბერი – 15 თებერვალი",
            dailyLimit = 5,
            status = "დაშვებულია (სეზონზე)",
            prohibitedMethods = listOf(
                "საგაზაფხულო ნადირობა (ტიაგა) კატეგორიულად აკრძალულია",
                "ღამით ფარებით ნადირობა"
            ),
            description = "იდუმალი, ღამის ცხოვრების მოყვარული ფრინველი გრძელი ნისკარტით. სანადირო ოსტატობის ნამდვილი გამოცდა.",
            habitat = "ნესტიანი ფოთლოვანი და შერეული ტყეები, ბუჩქნარები, ნაკადულების პირები.",
            huntingTips = "ნადირობა მეძებარი ძაღლით შემოდგომის და ზამთრის გადაფრენისას. საჭიროა სწრაფი და ზუსტი სროლა რთულ რელიეფზე.",
            identification = "მკვრივი სხეული, გრძელი სწორი ნისკარტი, დამცავი მუქი-ყავისფერი შეფერილობა.",
            legalStatus = "დღიური ლიმიტი: 5 ცალი. საგაზაფხულო ნადირობა აკრძალულია კანონით.",
            isProtected = false
        ),
        // 6. გარეული ბატი
        SpeciesEntity(
            id = 6,
            nameGeo = "გარეული ბატი",
            scientificName = "Anser anser",
            category = "წყალმცურავი ფრინველი",
            isSeasonOpen = false,
            seasonDates = "1 ნოემბერი – 1 მარტი",
            dailyLimit = 3,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "ელექტრონული მანოკები",
                "ღამით განათებით ნადირობა",
                "ძრავიანი წყლის ტრანსპორტით დევნა"
            ),
            description = "დიდი ზომის ფრთხილი წყალმცურავი ფრინველი. გადაფრენისას ქმნის დამახასიათებელ სოლს.",
            habitat = "დიდი ტბები, წყალსაცავები, ჭაობიანი დაბლობები, ზამთრის ნათესები.",
            huntingTips = "მოითხოვს უმაღლესი დონის შენიღბვას, ორმო-საფარებს, ფიტულების დიდ გუნდს და ხარისხიან მანოკს.",
            identification = "დიდი ზომა, რუხი-ნაცრისფერი ბუმბული, ნარინჯისფერი ნისკარტი და ფეხები.",
            legalStatus = "დღიური ლიმიტი: 3 ცალი. ნადირობა მკაცრად რეგულირდება.",
            isProtected = false
        ),
        // 7. მელოტა (ლისუხა)
        SpeciesEntity(
            id = 7,
            nameGeo = "მელოტა (ლისუხა)",
            scientificName = "Fulica atra",
            category = "წყალმცურავი ფრინველი",
            isSeasonOpen = false,
            seasonDates = "1 ნოემბერი – 1 მარტი",
            dailyLimit = 6,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "ძრავიანი ნავით დევნა",
                "საფანტის უკონტროლო სროლა წყლის ზედაპირზე"
            ),
            description = "წყალმცურავი ფრინველი ლაკლაკასებრთა ოჯახიდან. ხშირად გვხვდება გუნდებად წყალსაცავებზე.",
            habitat = "ლელიანი და ლერწმიანი ტბები, წყალსაცავები, წყნარი მდინარეები.",
            huntingTips = "ჩასაფრება ლერწმის საფარში, დილის გადაფრენისას წყლის ობიექტებს შორის.",
            identification = "მქრქალი შავი შეფერილობა, დამახასიათებელი თეთრი ნისკარტი და თეთრი შუბლის ფარი.",
            legalStatus = "დღიური ლიმიტი: 6 ცალი ერთ მონადირეზე.",
            isProtected = false
        ),
        // 8. კოლხური ხოხობი
        SpeciesEntity(
            id = 8,
            nameGeo = "კოლხური ხოხობი",
            scientificName = "Phasianus colchicus",
            category = "ფრინველები",
            isSeasonOpen = false,
            seasonDates = "სპეციალური კვოტით / სანადირო მეურნეობები",
            dailyLimit = 2,
            status = "სალიცენზიო",
            prohibitedMethods = listOf(
                "ულიცენზიო ნადირობა",
                "დედლის (დედალი ხოხბის) მოპოვება",
                "ხაფანგები და ბადეები"
            ),
            description = "საქართველოს ენდემური სიამაყე. ძლიერი და ფრთხილი ფრინველი.",
            habitat = "ჭალის ტყეები, ეკალ-ბარდები, მდინარეთა ხეობები.",
            huntingTips = "მოითხოვს გაწვრთნილ სპანიელს ან მეძებარს ხშირ ეკალ-ბარდში სამუშაოდ.",
            identification = "მამალი გამოირჩევა ოქროსფერ-მოწითალო ბუმბულითა და გრძელი კუდით.",
            legalStatus = "ნადირობა დაშვებულია მხოლოდ სპეციალური კვოტით ან სანადირო მეურნეობებში.",
            isProtected = false
        ),
        // 9. გარეული ღორი
        SpeciesEntity(
            id = 9,
            nameGeo = "გარეული ღორი (ტახი)",
            scientificName = "Sus scrofa",
            category = "ჩლიქოსნები",
            isSeasonOpen = false,
            seasonDates = "სპეციალური სალიცენზიო სეზონი",
            dailyLimit = 1,
            status = "სალიცენზიო",
            prohibitedMethods = listOf(
                "ღამის ოპტიკა და თერმოვიზორი (დაუკითხავად)",
                "ხაფანგები, მარყუჟები, ორმოები",
                "სატრანსპორტო საშუალებიდან დევნა"
            ),
            description = "დიდი და ძლიერი ცხოველი. ერთ-ერთი ყველაზე ემოციური და საპასუხისმგებლო სანადირო ობიექტი.",
            habitat = "ხშირი ტყეები, მუხნარ-წიფლნარები, ლელიანები და სასოფლო-სამეურნეო სავარგულების მიმდებარე ტყეები.",
            huntingTips = "ნადირობა კოლექტიური მორეკვით (ნომრებში დგომა) ან კოშკურებიდან კვების ადგილებზე.",
            identification = "მასიური სხეული, მუქი ჯაგარი, მამლებს განვითარებული აქვთ ეშვები.",
            legalStatus = "სავალდებულოა სპეციალური ლიცენზია ან სანადირო მეურნეობის საგზური.",
            isProtected = false
        ),
        // 10. კავკასიური შველი
        SpeciesEntity(
            id = 10,
            nameGeo = "კავკასიური შველი",
            scientificName = "Capreolus capreolus",
            category = "ჩლიქოსნები",
            isSeasonOpen = false,
            seasonDates = "სალიცენზიო პერიოდი",
            dailyLimit = 1,
            status = "სალიცენზიო",
            prohibitedMethods = listOf(
                "ულიცენზიო მოპოვება (ისჯება სისხლის სამართლის წესით)",
                "მარყუჟები და საწამლავი"
            ),
            description = "მცირე ზომის ელეგანტური ირმისებრი. გამოირჩევა მახვილი სმენით და ყნოსვით.",
            habitat = "შერეული და ფოთლოვანი ტყეები, ტყისპირა მდელოები, ხეობები.",
            huntingTips = "ჩასაფრებით ან მიპარვით ნადირობა ადრეულ დილას.",
            identification = "მოწითალო-ყავისფერი ზაფხულში, რუხი ზამთარში, მოკლე რქები 3-3 განშტოებით.",
            legalStatus = "მკაცრად რეგულირდება ლიცენზიით.",
            isProtected = false
        ),
        // 11. ტურა
        SpeciesEntity(
            id = 11,
            nameGeo = "ტურა",
            scientificName = "Canis aureus",
            category = "მტაცებლები",
            isSeasonOpen = true,
            seasonDates = "მთელი წლის განმავლობაში (დადგენილი წესით)",
            dailyLimit = 0,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "საწამლავის გამოყენება",
                "დასახლებულ პუნქტთან 500მ-ზე ახლოს სროლა"
            ),
            description = "მცირე ზომის მტაცებელი, რომელიც ხშირად აზიანებს ფაუნასა და სოფლის მეურნეობას.",
            habitat = "ბუჩქნარები, ჭალის ტყეები, სოფლების მიმდებარე ტერიტორიები.",
            huntingTips = "მანოკით (ხმის იმიტატორით) მოტყუება საღამოს და ღამის საათებში.",
            identification = "მელიაზე დიდი, მგელზე პატარა, მოყვითალო-რუხი შეფერილობა.",
            legalStatus = "რაოდენობის რეგულირება დაშვებულია კანონმდებლობის ფარგლებში.",
            isProtected = false
        ),
        // 12. მელა
        SpeciesEntity(
            id = 12,
            nameGeo = "მელა",
            scientificName = "Vulpes vulpes",
            category = "მტაცებლები",
            isSeasonOpen = true,
            seasonDates = "შემოდგომა - ზამთარი",
            dailyLimit = 0,
            status = "დაშვებულია",
            prohibitedMethods = listOf(
                "საწამლავები",
                "სოროების გათხრა ან აფეთქება"
            ),
            description = "ფართოდ გავრცელებული მოხერხებული მტაცებელი.",
            habitat = "ტყეები, ველები, ბუჩქნარები მთელ საქართველოში.",
            huntingTips = "ჩასაფრება სოროებთან ან თოვლზე კვალის მიხედვით ძებნა.",
            identification = "წითური ბეწვი, ფუმფულა თეთრბოლოიანი კუდი, წამახვილებული დრუნჩი.",
            legalStatus = "დაშვებულია სანადირო წესების დაცვით.",
            isProtected = false
        ),
        // 13. რუხი მგელი
        SpeciesEntity(
            id = 13,
            nameGeo = "რუხი მგელი",
            scientificName = "Canis lupus",
            category = "მტაცებლები",
            isSeasonOpen = false,
            seasonDates = "სპეციალური ნებართვით",
            dailyLimit = 0,
            status = "სპეციალური ნებართვა",
            prohibitedMethods = listOf(
                "საწამლავები",
                "ავიაციიდან ან ავტომობილიდან დევნა ნებართვის გარეშე"
            ),
            description = "უმაღლესი ინტელექტის მქონე მტაცებელი. რეგულირდება ეკოსისტემისა და შინაური პირუტყვის უსაფრთხოების მიზნით.",
            habitat = "მთიანი რეგიონები, ტყე-სტეპები, ნახევრადუდაბნოები.",
            huntingTips = "მოითხოვს უმაღლეს სანადირო გამოცდილებას, კვალზე სიარულს ან სპეციალურ სატყუარას.",
            identification = "დიდი ზომის ძაღლისებრი, ძლიერი ყბები, ფართო შუბლი, რუხი-მონაცრისფრო შეფერილობა.",
            legalStatus = "რეგულირდება გარემოს დაცვის სამინისტროს ნორმატიული აქტებით.",
            isProtected = false
        ),
        // 14. რუხი კურდღელი
        SpeciesEntity(
            id = 14,
            nameGeo = "რუხი კურდღელი",
            scientificName = "Lepus europaeus",
            category = "სხვა ნადირი",
            isSeasonOpen = false,
            seasonDates = "ნოემბერი – იანვარი",
            dailyLimit = 2,
            status = "დაშვებულია (სეზონზე)",
            prohibitedMethods = listOf(
                "ღამით ფარებით ნადირობა",
                "ავტომობილით დევნა",
                "მარყუჟები"
            ),
            description = "სწრაფი და ფრთხილი ბალახისმჭამელი. კლასიკური სანადირო ობიექტი მეძებარი და მადევარი ძაღლებით.",
            habitat = "ღია ველები, ბუჩქნარები, ტყისპირები, მთისწინეთი.",
            huntingTips = "მადევარი ძაღლებით (გონჩი) ნადირობა წრეზე, ან ზამთარში კვალზე თვალთვალი.",
            identification = "გრძელი ყურები შავი ბოლოებით, გრძელი უკანა კიდურები.",
            legalStatus = "დღიური ლიმიტი: 2 ცალი.",
            isProtected = false
        )
    )

    val sampleSpots = listOf(
        HuntingSpotEntity(
            id = 1,
            name = "ბორჯომის ტყისპირა პუნქტი #1",
            category = "სანადირო ადგილი",
            latitude = 41.8383,
            longitude = 43.3792,
            elevationMeters = 890,
            notes = "კარგი ადგილია ქედანზე და ტყის ქათამზე. დილით ადრე კარგი გადაფრენაა.",
            isFavorite = true
        ),
        HuntingSpotEntity(
            id = 2,
            name = "ალგეთის ხეობის ცივი წყარო",
            category = "წყარო",
            latitude = 41.6782,
            longitude = 44.4215,
            elevationMeters = 1120,
            notes = "სასმელი წყლის სუფთა წყარო. ახლოს არის მოსასვენებელი ადგილი.",
            isFavorite = true
        ),
        HuntingSpotEntity(
            id = 3,
            name = "ვაშლოვანის მიჯნისყურის ბანაკი",
            category = "კარავი",
            latitude = 41.2185,
            longitude = 46.5298,
            elevationMeters = 340,
            notes = "უსაფრთხო საბანაკე ადგილი. ცეცხლის დანთება მხოლოდ გამოყოფილ ადგილას.",
            isFavorite = false
        ),
        HuntingSpotEntity(
            id = 4,
            name = "ლაგოდეხის ზედა ბილიკი - კლდოვანი მონაკვეთი",
            category = "საფრთხე",
            latitude = 41.8267,
            longitude = 46.2842,
            elevationMeters = 1450,
            notes = "ყურადღება! ნაწვიმარზე სრიალა კლდოვანი დაღმართი. საჭიროა სიფრთხილე.",
            isFavorite = false
        ),
        HuntingSpotEntity(
            id = 5,
            name = "საგარეჯოს მწყრის ველები",
            category = "სანადირო ადგილი",
            latitude = 41.7335,
            longitude = 45.3312,
            elevationMeters = 720,
            notes = "აგვისტო-სექტემბრის მწყრის საუკეთესო ლოკაცია. მეძებარ ძაღლთან ერთად იდეალურია.",
            isFavorite = true
        )
    )

    val sampleEquipment = listOf(
        EquipmentEntity(
            id = 1,
            name = "ორლულიანი თოფი Beretta 686 Silver Pigeon",
            category = "თოფი",
            brand = "Beretta",
            model = "686 Silver Pigeon I (12/76)",
            serialNumber = "U58291B",
            purchaseDate = "2023-04-15",
            lastMaintenanceDate = "2026-08-20",
            nextMaintenanceDate = "2026-09-20",
            notes = "ლულები გაპოხილია, ჩოკები შემოწმებულია (0.25 / 0.50).",
            isReminderEnabled = true
        ),
        EquipmentEntity(
            id = 2,
            name = "ოპტიკური სამიზნე Vortex Crossfire II",
            category = "ოპტიკა",
            brand = "Vortex",
            model = "3-9x40 Dead-Hold BDC",
            serialNumber = "CF2-31007",
            purchaseDate = "2024-02-10",
            lastMaintenanceDate = "2026-07-15",
            nextMaintenanceDate = "2026-10-01",
            notes = "100 მეტრზე მისროლილია, ლინზები გასუფთავებულია.",
            isReminderEnabled = true
        ),
        EquipmentEntity(
            id = 3,
            name = "სანადირო ჩექმა Meindl Dovre Extreme GTX",
            category = "ფეხსაცმელი",
            brand = "Meindl",
            model = "Dovre Extreme MFS Wide",
            serialNumber = "N/A",
            purchaseDate = "2023-11-01",
            lastMaintenanceDate = "2026-08-10",
            nextMaintenanceDate = "2026-11-01",
            notes = "დამუშავებულია წყალგაუმტარი ცვილით.",
            isReminderEnabled = false
        ),
        EquipmentEntity(
            id = 4,
            name = "Garmin Alpha 100 & TT15 GPS ძაღლის საყელური",
            category = "GPS",
            brand = "Garmin",
            model = "Alpha 100",
            serialNumber = "3DR09281",
            purchaseDate = "2024-08-01",
            lastMaintenanceDate = "2026-08-28",
            nextMaintenanceDate = "2026-09-15",
            notes = "ბატარეები 100%-ზეა დამუხტული. საქართველოს ტოპო რუკა ჩატვირთულია.",
            isReminderEnabled = true
        )
    )

    val sampleTrips = listOf(
        HuntingTripEntity(
            id = 1,
            title = "მწყერზე ნადირობა საგარეჯოს ველებზე",
            date = "2026-08-29",
            startTime = "06:15",
            endTime = "10:30",
            durationMinutes = 255,
            locationName = "საგარეჯოს მინდვრები",
            latitude = 41.7335,
            longitude = 45.3312,
            weatherSummary = "მზიანი, სუსტი ნიავი",
            temperatureC = 19,
            windKmh = 8,
            windDirection = "აღმოსავლეთი",
            huntingType = "ფრინველზე ნადირობა",
            targetSpecies = "მწყერი",
            hunterCount = 2,
            equipmentUsed = "Beretta 686 (12 ყალიბი)",
            ammoUsed = "28გრ N9 (20 ვაზნა)",
            isSuccessful = true,
            harvestCount = 9,
            harvestDetails = "9 ცალი მწყერი",
            notes = "ძაღლმა ძალიან კარგად იმუშავა, ნაბული არ გაუფუჭებია. დილის 8 საათისთვის გადაფრენა დასრულდა.",
            photoUrl = "",
            isSynced = true
        ),
        HuntingTripEntity(
            id = 2,
            title = "ტყის ქათამზე გასვლა ბორჯომში",
            date = "2026-08-15",
            startTime = "07:00",
            endTime = "12:00",
            durationMinutes = 300,
            locationName = "ბორჯომის ფერდობები",
            latitude = 41.8383,
            longitude = 43.3792,
            weatherSummary = "მოღრუბლული, ნოტიო",
            temperatureC = 14,
            windKmh = 14,
            windDirection = "ჩრდილოეთი",
            huntingType = "ტყის ფრინველზე",
            targetSpecies = "ტყის ქათამი",
            hunterCount = 1,
            equipmentUsed = "ორლულიანი 12 ყალიბი",
            ammoUsed = "32გრ N7.5 (10 ვაზნა)",
            isSuccessful = true,
            harvestCount = 2,
            harvestDetails = "2 ცალი ტყის ქათამი",
            notes = "ხშირ ტყეში რთული სასროლი პოზიციები იყო, თუმცა 2 ზუსტი გასროლა შედგა.",
            photoUrl = "",
            isSynced = true
        )
    )

    val sampleNotifications = listOf(
        NotificationEntity(
            id = 1,
            title = "იარაღის ტექნიკური შემოწმება",
            message = "თქვენს Beretta 686-ს უახლოვდება გეგმიური წმენდის თარიღი (20 სექტემბერი).",
            type = "MAINTENANCE",
            timestamp = System.currentTimeMillis() - 3600000 * 4,
            isRead = false
        ),
        NotificationEntity(
            id = 2,
            title = "სანადირო სეზონის შეხსენება",
            message = "მწყერზე ნადირობის აქტიური პერიოდი მიმდინარეობს. გადაამოწმეთ მოსაკრებლის ქვითარი.",
            type = "SEASON",
            timestamp = System.currentTimeMillis() - 3600000 * 24,
            isRead = false
        ),
        NotificationEntity(
            id = 3,
            title = "ამინდის ცვლილება რეგიონში",
            message = "ხვალ დილით ბორჯომის მიდამოებში მოსალოდნელია წნევის მატება და ხელსაყრელი ნადირობის პირობები.",
            type = "WEATHER",
            timestamp = System.currentTimeMillis() - 3600000 * 48,
            isRead = true
        )
    )

    val sampleChecklists = listOf(
        HuntingChecklistEntity(
            id = 1,
            title = "ფრინველზე ნადირობა (მწყერი, ქედანი, კაკაბი)",
            huntType = "BIRD_HUNTING",
            huntTypeLabelKa = "ფრინველზე ნადირობა",
            description = "მწყერზე, ქედანსა და კაკაბზე გასვლის სრული აღჭურვილობა, ვაზნები, ძაღლის ეკიპირება და საბუთები",
            targetSeason = "აგვისტო - თებერვალი",
            isPreset = true,
            createdAt = 1700000000000L
        ),
        HuntingChecklistEntity(
            id = 2,
            title = "დიდ ნადირზე ნადირობა (გარეული ღორი, ირემი)",
            huntType = "BIG_GAME",
            huntTypeLabelKa = "დიდი ნადირი (Big Game)",
            description = "ხრახნილლულიანი კარაბინი, მძიმე ვაზნები, ოპტიკა, რადიოკავშირი და ნადავლის გასატანი ჩანთები",
            targetSeason = "ოქტომბერი - იანვარი",
            isPreset = true,
            createdAt = 1700000001000L
        ),
        HuntingChecklistEntity(
            id = 3,
            title = "წყალმცურავ ფრინველებზე (იხვი, ბატი)",
            huntType = "WATERFOWL",
            huntTypeLabelKa = "წყალმცურავი ფრინველი",
            description = "ჭაობისა და ტბის ნადირობა: სატყუარები (Decoys), მანოკები, წყალგაუმტარი კომბინეზონი (Waders)",
            targetSeason = "ნოემბერი - მარტი",
            isPreset = true,
            createdAt = 1700000002000L
        ),
        HuntingChecklistEntity(
            id = 4,
            title = "მტაცებლებზე ნადირობა (მელა, ტურა, მგელი)",
            huntType = "PREDATOR",
            huntTypeLabelKa = "მტაცებლები",
            description = "ჩასაფრება და მანოკებით მოზიდვა, სრული კამუფლირება, შტატივი და ზუსტი სროლა",
            targetSeason = "ნოემბერი - თებერვალი",
            isPreset = true,
            createdAt = 1700000003000L
        ),
        HuntingChecklistEntity(
            id = 5,
            title = "მაღალმთიანი ექსპედიცია (მთის ნადირობა)",
            huntType = "MOUNTAIN",
            huntTypeLabelKa = "მაღალმთიანი ექსპედიცია",
            description = "ალპური ზონა, რთული რელიეფი, ავტონომიური ლაშქრობა, კარავი და სატელიტური GPS",
            targetSeason = "სექტემბერი - ნოემბერი",
            isPreset = true,
            createdAt = 1700000004000L
        )
    )

    val sampleChecklistItems = listOf(
        // Checklist 1: Bird Hunting
        ChecklistItemEntity(
            id = 101,
            checklistId = 1,
            title = "ორლულიანი ან ნახევრად-ავტომატური თოფი 12/76",
            category = "იარაღი & ვაზნები",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "გაწმენდილი და შეზეთილი"
        ),
        ChecklistItemEntity(
            id = 102,
            checklistId = 1,
            title = "წვრილი საფანტის ვაზნები N9 / N8 (24-28გ)",
            category = "იარაღი & ვაზნები",
            quantity = "50-75 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "მწყერზე და ქედანზე"
        ),
        ChecklistItemEntity(
            id = 103,
            checklistId = 1,
            title = "სანადირო იარაღის რეგისტრაციის მოწმობა",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "ორიგინალი თან უნდა იქონიოთ"
        ),
        ChecklistItemEntity(
            id = 104,
            checklistId = 1,
            title = "გადამფრენ ფრინველებზე მოსაკრებლის ქვითარი (10 ლარი)",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "ბანკის ქვითარი ან ელექტრონული ვერსია"
        ),
        ChecklistItemEntity(
            id = 105,
            checklistId = 1,
            title = "ნარინჯისფერი სასიგნალო ქუდი ან ჟილეტი (Hi-Viz)",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "უსაფრთხოების მთავარი ელემენტი"
        ),
        ChecklistItemEntity(
            id = 106,
            checklistId = 1,
            title = "პირველადი დახმარების მინი-აფთიაქი (ტურნიკეტი, ბინტი)",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 კომპლექტი",
            isPacked = false,
            isMandatory = true,
            notes = "საველე გადაუდებელი დახმარებისთვის"
        ),
        ChecklistItemEntity(
            id = 107,
            checklistId = 1,
            title = "საველე პატრონტაში (ქამარზე)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = false,
            notes = "სწრაფი გადატენვისთვის"
        ),
        ChecklistItemEntity(
            id = 108,
            checklistId = 1,
            title = "მსუბუქი საველე ბათინკი და გეტრები",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 წყვილი",
            isPacked = false,
            isMandatory = true,
            notes = "ეკლიან ბუჩქნარში სასიარულოდ"
        ),
        ChecklistItemEntity(
            id = 109,
            checklistId = 1,
            title = "სანადირო ჟილეტი ფრინველის ჯიბით (იაგდტაში)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "მოპოვებული ნადავლისთვის"
        ),
        ChecklistItemEntity(
            id = 110,
            checklistId = 1,
            title = "სანადირო ძაღლის GPS საყელური / ზანზალაკი",
            category = "ძაღლის აღჭურვილობა",
            quantity = "1 კომპლექტი",
            isPacked = true,
            isMandatory = false,
            notes = "ძაღლის ლოკაციისთვის მაღალ ბალახში"
        ),
        ChecklistItemEntity(
            id = 111,
            checklistId = 1,
            title = "წყლის დასალევი ჯამი და წყალი ძაღლისთვის (2ლ)",
            category = "ძაღლის აღჭურვილობა",
            quantity = "2 ლიტრი",
            isPacked = false,
            isMandatory = true,
            notes = "ძაღლის გაუწყლოების თავიდან ასაცილებლად"
        ),
        ChecklistItemEntity(
            id = 112,
            checklistId = 1,
            title = "სასმელი წყალი მონადირისთვის (2ლ) + თერმოსი",
            category = "ბანაკი & კვება",
            quantity = "2 ლიტრი",
            isPacked = false,
            isMandatory = true,
            notes = "ცხელ ამინდში აუცილებელია"
        ),

        // Checklist 2: Big Game
        ChecklistItemEntity(
            id = 201,
            checklistId = 2,
            title = "ხრახნილლულიანი კარაბინი (.308 Win / 30-06 / 7.62)",
            category = "იარაღი & ვაზნები",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "ოპტიკა გასწორებული 100მ-ზე"
        ),
        ChecklistItemEntity(
            id = 202,
            checklistId = 2,
            title = "მძიმე ექსპანსიური ვაზნები / ტყვიები (165-180 gr)",
            category = "იარაღი & ვაზნები",
            quantity = "20-40 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "დიდ ნადირზე"
        ),
        ChecklistItemEntity(
            id = 203,
            checklistId = 2,
            title = "დიდ ნადირზე ნადირობის ოფიციალური ლიცენზია და საგზური",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 კომპლექტი",
            isPacked = false,
            isMandatory = true,
            notes = "კანონით სავალდებულო"
        ),
        ChecklistItemEntity(
            id = 204,
            checklistId = 2,
            title = "ნათელი ნარინჯისფერი ჟილეტი (Hi-Viz Blaze Orange)",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "კოლექტიურ ნადირობაში უმთავრესი"
        ),
        ChecklistItemEntity(
            id = 205,
            checklistId = 2,
            title = "ტაქტიკური ტრავმა-აფთიაქი (IFAK + ტურნიკეტი CAT)",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 ნაკრები",
            isPacked = true,
            isMandatory = true,
            notes = "სისხლდენის შემჩერებელი საშუალებები"
        ),
        ChecklistItemEntity(
            id = 206,
            checklistId = 2,
            title = "ოპტიკური სამიზნე და ლინზების საწმენდი",
            category = "ოპტიკა & აქსესუარები",
            quantity = "1 ცალი",
            isPacked = true,
            isMandatory = true,
            notes = "მზის და წვიმისგან დამცავი ხუფებით"
        ),
        ChecklistItemEntity(
            id = 207,
            checklistId = 2,
            title = "სანადირო ბინოკლი (8x42 ან 10x42)",
            category = "ოპტიკა & აქსესუარები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "ფართო ხედვის კუთხით"
        ),
        ChecklistItemEntity(
            id = 208,
            checklistId = 2,
            title = "VHF რადიოსადგური (რაცია) + სათადარიგო ბატარეა",
            category = "ნავიგაცია & კავშირი",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ჯგუფთან კავშირისთვის უღრან ტყეში"
        ),
        ChecklistItemEntity(
            id = 209,
            checklistId = 2,
            title = "GPS ნავიგატორი + ტოპო რუკა დატვირთული",
            category = "ნავიგაცია & კავშირი",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ოფლაინ რეჟიმში მომუშავე"
        ),
        ChecklistItemEntity(
            id = 210,
            checklistId = 2,
            title = "სანადირო დანების კომპლექტი და სალესი",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 ნაკრები",
            isPacked = false,
            isMandatory = true,
            notes = "ხორცის დასამუშავებლად"
        ),
        ChecklistItemEntity(
            id = 211,
            checklistId = 2,
            title = "ძლიერი ფანარი თავზე (Headlamp) + 2000lm ფანარი",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "2 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "დაღამებისას უსაფრთხო გადაადგილებისთვის"
        ),
        ChecklistItemEntity(
            id = 212,
            checklistId = 2,
            title = "მემბრანული წყალგაუმტარი ქურთუკი (Gore-Tex)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ქარისგან და წვიმისგან დასაცავად"
        ),

        // Checklist 3: Waterfowl
        ChecklistItemEntity(
            id = 301,
            checklistId = 3,
            title = "თოფი 12/76 (მაგნუმი წყალგაუმტარი საფარით)",
            category = "იარაღი & ვაზნები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ტენიან გარემოში გამძლე"
        ),
        ChecklistItemEntity(
            id = 302,
            checklistId = 3,
            title = "ვაზნები N5 / N4 / N3 (32-36გრ)",
            category = "იარაღი & ვაზნები",
            quantity = "50 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ჭაობისა და ტბის გადაფრენებზე"
        ),
        ChecklistItemEntity(
            id = 303,
            checklistId = 3,
            title = "ნეოპრენის წყალგაუმტარი კომბინეზონი (Waders 4-5მმ)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ცივ წყალში დგომისთვის"
        ),
        ChecklistItemEntity(
            id = 304,
            checklistId = 3,
            title = "იხვის მცურავი სატყუარები (Decoys / ფიტულები)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "12-18 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "ტბაზე განლაგებისთვის"
        ),
        ChecklistItemEntity(
            id = 305,
            checklistId = 3,
            title = "იხვის აკუსტიკური მანოკი (Call)",
            category = "იარაღი & ვაზნები",
            quantity = "2 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "გადამფრენი გუნდის მოსაზიდად"
        ),
        ChecklistItemEntity(
            id = 306,
            checklistId = 3,
            title = "კამუფლირებული ჩასაფრების ბადე (Blind)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "ლერწამში შენიღბვისთვის"
        ),
        ChecklistItemEntity(
            id = 307,
            checklistId = 3,
            title = "ჰერმეტული ჩანთა (Dry Bag) ტელეფონისა და საბუთებისთვის",
            category = "უსაფრთხოება & საბუთები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "100% წყალგაუმტარი"
        ),

        // Checklist 4: Predator
        ChecklistItemEntity(
            id = 401,
            checklistId = 4,
            title = "კარაბინი .223 Rem ან 12 კალ. მსხვილი საფანტი (00)",
            category = "იარაღი & ვაზნები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "მელაზე და ტურაზე"
        ),
        ChecklistItemEntity(
            id = 402,
            checklistId = 4,
            title = "მანოკები (დაჭრილი კურდღლის / მღრღნელის ხმა)",
            category = "იარაღი & ვაზნები",
            quantity = "2 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "აკუსტიკური სატყუარა"
        ),
        ChecklistItemEntity(
            id = 403,
            checklistId = 4,
            title = "სასროლი საყრდენი შტატივი (Shooting Sticks / Bipod)",
            category = "ოპტიკა & აქსესუარები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = false,
            notes = "სტაბილური პოზიციისთვის"
        ),
        ChecklistItemEntity(
            id = 404,
            checklistId = 4,
            title = "სრული 3D კამუფლირება (ნიღაბი, ხელთათმანები, ხალათი)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 კომპლექტი",
            isPacked = false,
            isMandatory = true,
            notes = "მტაცებლის მახვილი მხედველობისგან დასამალად"
        ),

        // Checklist 5: Mountain Expedition
        ChecklistItemEntity(
            id = 501,
            checklistId = 5,
            title = "ზუსტი შორი მანძილის კარაბინი (.300 Win Mag / 6.5 Creedmoor)",
            category = "იარაღი & ვაზნები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "მაღალმთიან რელიეფზე"
        ),
        ChecklistItemEntity(
            id = 502,
            checklistId = 5,
            title = "მთის სალაშქრო ხისტი ბათინკები (Vibram)",
            category = "ტანსაცმელი & ეკიპირება",
            quantity = "1 წყვილი",
            isPacked = false,
            isMandatory = true,
            notes = "კლდოვან ფერდობებზე სასიარულოდ"
        ),
        ChecklistItemEntity(
            id = 503,
            checklistId = 5,
            title = "ლაზერული მანძილმზომი კუთხის კომპენსაციით",
            category = "ოპტიკა & აქსესუარები",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "ფერდობზე სროლის ბალისტიკისთვის"
        ),
        ChecklistItemEntity(
            id = 504,
            checklistId = 5,
            title = "ექსპედიციური ზურგჩანთა (60-70L) + კარავი და საძილე ტომარა",
            category = "ბანაკი & კვება",
            quantity = "1 კომპლექტი",
            isPacked = false,
            isMandatory = true,
            notes = "ავტონომიური ღამისთევისთვის"
        ),
        ChecklistItemEntity(
            id = 505,
            checklistId = 5,
            title = "წყლის გამფილტრავი სისტემა (Sawyer / Katadyn)",
            category = "ბანაკი & კვება",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "მთის ნაკადულებიდან წყლის მისაღებად"
        ),
        ChecklistItemEntity(
            id = 506,
            checklistId = 5,
            title = "სატელიტური SOS ტრეკერი / GPS",
            category = "ნავიგაცია & კავშირი",
            quantity = "1 ცალი",
            isPacked = false,
            isMandatory = true,
            notes = "კავშირგარეშე ზონაში უსაფრთხოებისთვის"
        )
    )
}
