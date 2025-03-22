package com.android.doctorcube.university.model;


import com.android.doctorcube.R;

import java.util.HashMap;
import java.util.Map;

public class UniversityDetailsData {

    // Custom class to hold detailed university data
    public static class UniversityDetail {
        private int imageResourceId; // University image (default: flag_china)
        private String location; // City, Country
        private String description; // Overview text
        private String established; // Year established
        private String ranking; // Ranking info
        private String address; // Full address
        private String phone; // Contact phone
        private String email; // Contact email

        public UniversityDetail(int imageResourceId, String location, String description, String established,
                                String ranking, String address, String phone, String email) {
            this.imageResourceId = imageResourceId;
            this.location = location;
            this.description = description;
            this.established = established;
            this.ranking = ranking;
            this.address = address;
            this.phone = phone;
            this.email = email;
        }

        // Getters
        public int getImageResourceId() { return imageResourceId; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
        public String getEstablished() { return established; }
        public String getRanking() { return ranking; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }

    // HashMap to store university details
    private static final Map<String, UniversityDetail> universityDetailsMap = new HashMap<>();

    static {
        // Russia (10 Universities)
        universityDetailsMap.put("Sechenov University", new UniversityDetail(
                R.drawable.flag_china,
                "Moscow, Russia",
                "Sechenov University, a leading public institution, offers General Medicine in English with a 6-year duration. Rated A+, it ranks in the Top 500 globally.",
                "1918",
                "Top 500",
                "Trubetskaya St, 8, Moscow, Russia",
                "+7 (495) 609-1400",
                "admissions@sechenov.edu"
        ));
        universityDetailsMap.put("Moscow State University of Medicine and Dentistry", new UniversityDetail(
                R.drawable.flag_china,
                "Moscow, Russia",
                "Specializing in Dentistry, this public university provides a 6-year English program and is ranked in the Top 500.",
                "1922",
                "Top 500",
                "Delegatskaya St, 20/1, Moscow, Russia",
                "+7 (495) 681-3747",
                "admissions@msmsu.edu"
        ));
        universityDetailsMap.put("Kazan Federal University", new UniversityDetail(
                R.drawable.flag_china,
                "Kazan, Russia",
                "Kazan Federal University offers a 6-year MBBS program in English, recognized in the Top 600 globally.",
                "1804",
                "Top 600",
                "Kremlyovskaya St, 18, Kazan, Russia",
                "+7 (843) 233-7100",
                "admissions@kfu.edu"
        ));
        universityDetailsMap.put("Novosibirsk State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Novosibirsk, Russia",
                "This public university provides a 6-year General Medicine program in English, ranked in the Top 800.",
                "1935",
                "Top 800",
                "Krasny Prospekt, 52, Novosibirsk, Russia",
                "+7 (383) 222-3200",
                "admissions@nsmu.edu"
        ));
        universityDetailsMap.put("Saint Petersburg State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Saint Petersburg, Russia",
                "Known for its Pediatrics program, this 6-year English course is offered at a Top 700 ranked public institution.",
                "1897",
                "Top 700",
                "Litovskaya St, 2, Saint Petersburg, Russia",
                "+7 (812) 295-0600",
                "admissions@spbsmu.edu"
        ));
        universityDetailsMap.put("Volgograd State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Volgograd, Russia",
                "Offering a 5-year Pharmacy program in English, this public university ranks in the Top 1000.",
                "1935",
                "Top 1000",
                "Pavshikh Bortsov Sq, 1, Volgograd, Russia",
                "+7 (8442) 38-5050",
                "admissions@volgmu.edu"
        ));
        universityDetailsMap.put("Rostov State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Rostov-on-Don, Russia",
                "A 6-year General Medicine program in English at a Top 900 ranked public university.",
                "1930",
                "Top 900",
                "Nakhichevansky Ln, 29, Rostov-on-Don, Russia",
                "+7 (863) 250-4200",
                "admissions@rostgmu.edu"
        ));
        universityDetailsMap.put("Ural State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Yekaterinburg, Russia",
                "This public university offers a 6-year Dentistry program in English, ranked in the Top 1200.",
                "1930",
                "Top 1200",
                "Repina St, 3, Yekaterinburg, Russia",
                "+7 (343) 214-8700",
                "admissions@usmu.edu"
        ));
        universityDetailsMap.put("Bashkir State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Ufa, Russia",
                "A Top 800 ranked public university offering a 6-year MBBS program in English.",
                "1932",
                "Top 800",
                "Lenina St, 3, Ufa, Russia",
                "+7 (347) 273-6100",
                "admissions@bsmu.edu"
        ));
        universityDetailsMap.put("Omsk State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Omsk, Russia",
                "Offering General Medicine in English for 6 years, this public university ranks in the Top 1500.",
                "1920",
                "Top 1500",
                "Lenina St, 12, Omsk, Russia",
                "+7 (3812) 23-3200",
                "admissions@omskmed.edu"
        ));

        // Georgia (7 Universities)
        universityDetailsMap.put("Tbilisi State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "A Top 500 public university offering a 6-year MD program in English with European-standard education.",
                "1918",
                "Top 500",
                "Vazha-Pshavela Ave, 33, Tbilisi, Georgia",
                "+995 (32) 254-2450",
                "admissions@tsmu.edu"
        ));
        universityDetailsMap.put("Batumi Shota Rustaveli State University", new UniversityDetail(
                R.drawable.flag_china,
                "Batumi, Georgia",
                "This public university provides a 6-year MBBS program in English, ranked in the Top 700.",
                "1935",
                "Top 700",
                "Ninoshvili St, 35, Batumi, Georgia",
                "+995 (422) 27-1780",
                "admissions@bsu.edu"
        ));
        universityDetailsMap.put("David Tvildiani Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "A private institution offering a 6-year General Medicine program in English, ranked Top 600.",
                "1989",
                "Top 600",
                "Lublin St, 2a, Tbilisi, Georgia",
                "+995 (32) 251-6898",
                "admissions@dtmu.edu"
        ));
        universityDetailsMap.put("Caucasus International University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "This private university offers a 6-year Dentistry program in English, ranked in the Top 1000.",
                "1995",
                "Top 1000",
                "Chargali St, 73, Tbilisi, Georgia",
                "+995 (32) 261-1298",
                "admissions@ciu.edu"
        ));
        universityDetailsMap.put("Georgian National University SEU", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "A Top 800 private university offering a 6-year MBBS program in English.",
                "2001",
                "Top 800",
                "Tsinandali St, 9, Tbilisi, Georgia",
                "+995 (32) 290-0000",
                "admissions@seu.edu"
        ));
        universityDetailsMap.put("Akaki Tsereteli State University", new UniversityDetail(
                R.drawable.flag_china,
                "Kutaisi, Georgia",
                "This public university provides a 6-year General Medicine program in English, ranked Top 1200.",
                "1930",
                "Top 1200",
                "Tamar Mepe St, 59, Kutaisi, Georgia",
                "+995 (431) 24-3636",
                "admissions@atsu.edu"
        ));
        universityDetailsMap.put("Ivane Javakhishvili Tbilisi State University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "A Top 400 public university offering a 5-year Pharmacy program in English.",
                "1918",
                "Top 400",
                "University St, 1, Tbilisi, Georgia",
                "+995 (32) 222-6710",
                "admissions@tsu.edu"
        ));

        // Kazakhstan (8 Universities)
        universityDetailsMap.put("Kazakh National Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Almaty, Kazakhstan",
                "A Top 1000 public university offering a 5-year MBBS program in English.",
                "1931",
                "Top 1000",
                "Tole Bi St, 94, Almaty, Kazakhstan",
                "+7 (727) 292-0800",
                "admissions@kaznmu.edu"
        ));
        universityDetailsMap.put("Astana Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Nur-Sultan, Kazakhstan",
                "This public university offers a 5-year General Medicine program in English, ranked Top 800.",
                "1964",
                "Top 800",
                "Beibitshilik St, 49a, Nur-Sultan, Kazakhstan",
                "+7 (7172) 53-9400",
                "admissions@amu.edu"
        ));
        universityDetailsMap.put("Semey State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Semey, Kazakhstan",
                "A Top 1200 public university providing a 5-year Dentistry program in English.",
                "1953",
                "Top 1200",
                "Abay St, 103, Semey, Kazakhstan",
                "+7 (7222) 52-2050",
                "admissions@ssmu.edu"
        ));
        universityDetailsMap.put("Karaganda State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Karaganda, Kazakhstan",
                "Offering a 5-year MBBS program in English, this public university ranks in the Top 900.",
                "1950",
                "Top 900",
                "Gogol St, 40, Karaganda, Kazakhstan",
                "+7 (7212) 51-8970",
                "admissions@ksmu.edu"
        ));
        universityDetailsMap.put("South Kazakhstan Medical Academy", new UniversityDetail(
                R.drawable.flag_china,
                "Shymkent, Kazakhstan",
                "A Top 1500 public university offering a 5-year Pharmacy program in English.",
                "1979",
                "Top 1500",
                "Al-Farabi Sq, 1, Shymkent, Kazakhstan",
                "+7 (7252) 40-8200",
                "admissions@skma.edu"
        ));
        universityDetailsMap.put("West Kazakhstan Marat Ospanov Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Aktobe, Kazakhstan",
                "This public university provides a 5-year General Medicine program in English, ranked Top 1100.",
                "1957",
                "Top 1100",
                "Maresyev St, 68, Aktobe, Kazakhstan",
                "+7 (7132) 56-3400",
                "admissions@wkmu.edu"
        ));
        universityDetailsMap.put("Al-Farabi Kazakh National University", new UniversityDetail(
                R.drawable.flag_china,
                "Almaty, Kazakhstan",
                "A Top 600 public university offering a 5-year Pediatrics program in English.",
                "1934",
                "Top 600",
                "Al-Farabi Ave, 71, Almaty, Kazakhstan",
                "+7 (727) 377-3330",
                "admissions@al-farabi.edu"
        ));
        universityDetailsMap.put("Kokshetau State University", new UniversityDetail(
                R.drawable.flag_china,
                "Kokshetau, Kazakhstan",
                "Offering a 5-year MBBS program in English, this public university ranks in the Top 2000.",
                "1996",
                "Top 2000",
                "Abay St, 76, Kokshetau, Kazakhstan",
                "+7 (7162) 25-5100",
                "admissions@kokshetau.edu"
        ));

        // Nepal (6 Universities)
        universityDetailsMap.put("Kathmandu Medical College", new UniversityDetail(
                R.drawable.flag_china,
                "Kathmandu, Nepal",
                "A Top 1500 private institution offering a 5.5-year MBBS program in English.",
                "1997",
                "Top 1500",
                "Sinamangal, Kathmandu, Nepal",
                "+977 (1) 447-6152",
                "admissions@kmc.edu"
        ));
        universityDetailsMap.put("B.P. Koirala Institute of Health Sciences", new UniversityDetail(
                R.drawable.flag_china,
                "Dharan, Nepal",
                "This public university offers a 5.5-year General Medicine program in English, ranked Top 1000.",
                "1993",
                "Top 1000",
                "Ghopa Camp, Dharan, Nepal",
                "+977 (25) 525-555",
                "admissions@bpkihs.edu"
        ));
        universityDetailsMap.put("Tribhuvan University Institute of Medicine", new UniversityDetail(
                R.drawable.flag_china,
                "Kathmandu, Nepal",
                "A Top 800 public university providing a 5.5-year MBBS program in English.",
                "1972",
                "Top 800",
                "Maharajgunj, Kathmandu, Nepal",
                "+977 (1) 441-0911",
                "admissions@iom.edu"
        ));
        universityDetailsMap.put("Nepal Medical College", new UniversityDetail(
                R.drawable.flag_china,
                "Kathmandu, Nepal",
                "This private university offers a 5.5-year Dentistry program in English, ranked Top 2000.",
                "1997",
                "Top 2000",
                "Attarkhel, Jorpati, Kathmandu, Nepal",
                "+977 (1) 491-1008",
                "admissions@nmc.edu"
        ));
        universityDetailsMap.put("Manipal College of Medical Sciences", new UniversityDetail(
                R.drawable.flag_china,
                "Pokhara, Nepal",
                "A Top 1200 private institution offering a 5.5-year MBBS program in English.",
                "1994",
                "Top 1200",
                "Deep Heights, Pokhara, Nepal",
                "+977 (61) 440-600",
                "admissions@manipal.edu"
        ));
        universityDetailsMap.put("Patan Academy of Health Sciences", new UniversityDetail(
                R.drawable.flag_china,
                "Lalitpur, Nepal",
                "This public university provides a 5.5-year General Medicine program in English, ranked Top 900.",
                "2008",
                "Top 900",
                "Lagankhel, Lalitpur, Nepal",
                "+977 (1) 554-5112",
                "admissions@pahs.edu"
        ));

        // China (10 Universities)
        universityDetailsMap.put("Peking University", new UniversityDetail(
                R.drawable.flag_china,
                "Beijing, China",
                "A Top 100 public university offering a 5-year MBBS program in English.",
                "1898",
                "Top 100",
                "5 Yiheyuan Rd, Haidian, Beijing, China",
                "+86 (10) 6275-1201",
                "admissions@pku.edu"
        ));
        universityDetailsMap.put("Fudan University", new UniversityDetail(
                R.drawable.flag_china,
                "Shanghai, China",
                "This public university provides a 6-year MBBS program in English, ranked Top 50 globally.",
                "1905",
                "Top 50",
                "220 Handan Rd, Yangpu, Shanghai, China",
                "+86 (21) 6564-2222",
                "admissions@fudan.edu"
        ));
        universityDetailsMap.put("Zhejiang University", new UniversityDetail(
                R.drawable.flag_china,
                "Hangzhou, China",
                "A Top 200 public university offering a 6-year General Medicine program in English.",
                "1897",
                "Top 200",
                "866 Yuhangtang Rd, Hangzhou, China",
                "+86 (571) 8820-6114",
                "admissions@zju.edu"
        ));
        universityDetailsMap.put("Shanghai Jiao Tong University", new UniversityDetail(
                R.drawable.flag_china,
                "Shanghai, China",
                "This public university offers a 6-year Dentistry program in English, ranked Top 150.",
                "1896",
                "Top 150",
                "800 Dongchuan Rd, Minhang, Shanghai, China",
                "+86 (21) 3420-6000",
                "admissions@sjtu.edu"
        ));
        universityDetailsMap.put("Sun Yat-sen University", new UniversityDetail(
                R.drawable.flag_china,
                "Guangzhou, China",
                "A Top 300 public university providing a 6-year MBBS program in English.",
                "1924",
                "Top 300",
                "135 Xingang Xi Rd, Guangzhou, China",
                "+86 (20) 8411-2828",
                "admissions@sysu.edu"
        ));
        universityDetailsMap.put("Nanjing Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Nanjing, China",
                "This public university offers a 5-year Pharmacy program in English, ranked Top 400.",
                "1934",
                "Top 400",
                "101 Longmian Ave, Jiangning, Nanjing, China",
                "+86 (25) 8686-9120",
                "admissions@nmu.edu"
        ));
        universityDetailsMap.put("Tongji University", new UniversityDetail(
                R.drawable.flag_china,
                "Shanghai, China",
                "A Top 250 public university providing a 6-year General Medicine program in English.",
                "1907",
                "Top 250",
                "1239 Siping Rd, Shanghai, China",
                "+86 (21) 6598-2200",
                "admissions@tongji.edu"
        ));
        universityDetailsMap.put("China Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Shenyang, China",
                "This public university offers a 6-year MBBS program in English, ranked Top 500.",
                "1931",
                "Top 500",
                "77 Puhe Rd, Shenbei, Shenyang, China",
                "+86 (24) 3193-9000",
                "admissions@cmu.edu"
        ));
        universityDetailsMap.put("Huazhong University of Science and Technology", new UniversityDetail(
                R.drawable.flag_china,
                "Wuhan, China",
                "A Top 200 public university offering a 6-year Pediatrics program in English.",
                "1953",
                "Top 200",
                "1037 Luoyu Rd, Wuhan, China",
                "+86 (27) 8754-2114",
                "admissions@hust.edu"
        ));
        universityDetailsMap.put("Sichuan University", new UniversityDetail(
                R.drawable.flag_china,
                "Chengdu, China",
                "This public university provides a 6-year Dentistry program in English, ranked Top 350.",
                "1896",
                "Top 350",
                "24 South Section 1, Yihuan Rd, Chengdu, China",
                "+86 (28) 8540-7100",
                "admissions@scu.edu"
        ));

        // Uzbekistan (6 Universities)
        universityDetailsMap.put("Samarkand State Medical Institute", new UniversityDetail(
                R.drawable.flag_china,
                "Samarkand, Uzbekistan",
                "A Top 2000 public university offering a 6-year General Medicine program in English.",
                "1930",
                "Top 2000",
                "Amir Temur St, 18, Samarkand, Uzbekistan",
                "+998 (66) 233-0662",
                "admissions@ssmi.edu"
        ));
        universityDetailsMap.put("Tashkent Medical Academy", new UniversityDetail(
                R.drawable.flag_china,
                "Tashkent, Uzbekistan",
                "This public university provides a 6-year MBBS program in English, ranked Top 1500.",
                "1919",
                "Top 1500",
                "Farobi St, 2, Tashkent, Uzbekistan",
                "+998 (71) 150-7800",
                "admissions@tma.edu"
        ));
        universityDetailsMap.put("Bukhara State Medical Institute", new UniversityDetail(
                R.drawable.flag_china,
                "Bukhara, Uzbekistan",
                "A Top 2500 public university offering a 6-year Dentistry program in English.",
                "1990",
                "Top 2500",
                "Navoi St, 1, Bukhara, Uzbekistan",
                "+998 (65) 223-0014",
                "admissions@bsmi.edu"
        ));
        universityDetailsMap.put("Andijan State Medical Institute", new UniversityDetail(
                R.drawable.flag_china,
                "Andijan, Uzbekistan",
                "This public university provides a 6-year General Medicine program in English, ranked Top 1800.",
                "1955",
                "Top 1800",
                "Yusuf Otabekov St, 1, Andijan, Uzbekistan",
                "+998 (74) 223-9400",
                "admissions@asmi.edu"
        ));
        universityDetailsMap.put("Fergana State University Medical Faculty", new UniversityDetail(
                R.drawable.flag_china,
                "Fergana, Uzbekistan",
                "A Top 2200 public university offering a 6-year MBBS program in English.",
                "1991",
                "Top 2200",
                "Murabbiylar St, 19, Fergana, Uzbekistan",
                "+998 (73) 224-4200",
                "admissions@fsu.edu"
        ));
        universityDetailsMap.put("Urgench Branch of Tashkent Medical Academy", new UniversityDetail(
                R.drawable.flag_china,
                "Urgench, Uzbekistan",
                "This public university provides a 5-year Pharmacy program in English, ranked Top 2000.",
                "1992",
                "Top 2000",
                "Al-Khorezmi St, 25, Urgench, Uzbekistan",
                "+998 (62) 224-0700",
                "admissions@urtma.edu"
        ));
    }

    // Method to get university details by name
    public static UniversityDetail getUniversityDetail(String universityName) {
        return universityDetailsMap.get(universityName);
    }
}