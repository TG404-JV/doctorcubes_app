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
        private String admissionRequirements; // Added admission requirements

        public UniversityDetail(int imageResourceId, String location, String description, String established,
                                String ranking, String address, String phone, String email, String admissionRequirements) {
            this.imageResourceId = imageResourceId;
            this.location = location;
            this.description = description;
            this.established = established;
            this.ranking = ranking;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.admissionRequirements = admissionRequirements; // Initialize
        }

        // Getters
        public int getImageResourceId() {
            return imageResourceId;
        }

        public String getLocation() {
            return location;
        }

        public String getDescription() {
            return description;
        }

        public String getEstablished() {
            return established;
        }

        public String getRanking() {
            return ranking;
        }

        public String getAddress() {
            return address;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getAdmissionRequirements() {
            return admissionRequirements;
        }
    }

    // HashMap to store university details
    private static final Map<String, UniversityDetail> universityDetailsMap = new HashMap<>();

    static {
        // Russia (10 Universities)
        universityDetailsMap.put("Sechenov University", new UniversityDetail(
                R.drawable.flag_china,
                "Moscow, Russia",
                "Sechenov University, established in 1758, is a leading medical institution in Russia and globally recognized for its extensive research, clinical practice, and academic excellence.  It offers a 6-year General Medicine program in English, emphasizing a holistic approach to healthcare and consistently ranks within the top 500 universities worldwide.  The university is known for its strong faculty, state-of-the-art facilities, and a commitment to innovation in medical education.",
                "1758",
                "Top 500",
                "Trubetskaya St, 8, Moscow, Russia",
                "+7 (495) 609-1400",
                "admissions@sechenov.edu",
                "High school diploma with strong grades in Biology, Chemistry, and English.  Entrance exam or interview may be required.  Proof of English proficiency (TOEFL/IELTS).  Minimum GPA of 3.0."
        ));
        universityDetailsMap.put("Moscow State University of Medicine and Dentistry", new UniversityDetail(
                R.drawable.flag_china,
                "Moscow, Russia",
                "Moscow State University of Medicine and Dentistry (MSMSU) is one of the oldest and most respected dental schools in Russia.  Founded in 1922, MSMSU provides a 5-year Dentistry program in English, focusing on advanced dental techniques, oral healthcare management, and maxillofacial surgery.  The university is equipped with modern clinics and laboratories, ensuring students receive practical, hands-on training.  MSMSU is also recognized for its contributions to dental research and its strong international collaborations, and is ranked in the top 500 globally.",
                "1922",
                "Top 500",
                "Delegatskaya St, 20/1, Moscow, Russia",
                "+7 (495) 681-3747",
                "admissions@msmsu.edu",
                "High school diploma with a focus on science subjects.  Successful completion of entrance exams.  English language proficiency test (IELTS/TOEFL).  GPA of 3.2 or higher."
        ));
        universityDetailsMap.put("Kazan Federal University", new UniversityDetail(
                R.drawable.flag_china,
                "Kazan, Russia",
                "Kazan Federal University (KFU), established in 1804, is one of Russia's oldest universities, with a rich history of academic excellence.  KFU offers a 6-year MBBS program in English, providing students with a comprehensive medical education.  The university is known for its strong emphasis on research, innovation, and a multidisciplinary approach to learning. KFU is globally recognized and is ranked within the top 600 universities.",
                "1804",
                "Top 600",
                "Kremlyovskaya St, 18, Kazan, Russia",
                "+7 (843) 233-7100",
                "admissions@kfu.edu",
                "Completed secondary education with qualifying grades.  Passage of university entrance examinations.  Evidence of English language skills (TOEFL/IELTS).  GPA of 3.1."
        ));
        universityDetailsMap.put("Novosibirsk State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Novosibirsk, Russia",
                "Novosibirsk State Medical University (NSMU), founded in 1935, is a leading medical university in Siberia. NSMU offers a 6-year General Medicine program in English, focusing on training highly qualified doctors. The university has a strong clinical base, modern facilities, and is actively involved in medical research. NSMU is ranked within the top 800 universities globally.",
                "1935",
                "Top 800",
                "Krasny Prospekt, 52, Novosibirsk, Russia",
                "+7 (383) 222-3200",
                "admissions@nsmu.edu",
                "High school certificate with good grades in sciences.  Successful interview performance.  IELTS or TOEFL score.  GPA of 3.0."
        ));
        universityDetailsMap.put("Saint Petersburg State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Saint Petersburg, Russia",
                "Saint Petersburg State Medical University (SPbSMU), established in 1897, is one of the oldest and most prestigious medical schools in Russia.  SPbSMU is particularly renowned for its pediatrics program and offers a 6-year English-taught MD program.  The university is committed to providing high-quality medical education, fostering research, and contributing to the advancement of medical science.  SPbSMU is ranked within the top 700 universities worldwide.",
                "1897",
                "Top 700",
                "Litovskaya St, 2, Saint Petersburg, Russia",
                "+7 (812) 295-0600",
                "admissions@spbsmu.edu",
                "Secondary school diploma with specialization in sciences.  University entrance test.  TOEFL/IELTS scores.  GPA of 3.3."
        ));
        universityDetailsMap.put("Volgograd State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Volgograd, Russia",
                "Volgograd State Medical University (VSMU), founded in 1935, is a prominent center for medical education and research in Southern Russia. VSMU offers a 5-year Pharmacy program in English, focusing on training specialists in the field of pharmacy. The university is known for its strong academic programs, clinical facilities, and contributions to pharmaceutical research. VSMU is ranked within the top 1000 universities.",
                "1935",
                "Top 1000",
                "Pavshikh Bortsov Sq, 1, Volgograd, Russia",
                "+7 (8442) 38-5050",
                "admissions@volgmu.edu",
                "High school diploma with chemistry and biology.  Entrance exam.  Proof of English language proficiency.  GPA of 3.0."
        ));
        universityDetailsMap.put("Rostov State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Rostov-on-Don, Russia",
                "Rostov State Medical University (RosSMU), established in 1930, is a major medical education and research center in Southern Russia. RosSMU offers a 6-year General Medicine program in English, providing students with a comprehensive medical education. The university is known for its clinical training, research activities, and contributions to healthcare in the region. RosSMU is ranked within the top 900 universities globally.",
                "1930",
                "Top 900",
                "Nakhichevansky Ln, 29, Rostov-on-Don, Russia",
                "+7 (863) 250-4200",
                "admissions@rostgmu.edu",
                "Secondary education certificate.  Entrance examination.  IELTS or equivalent.  GPA of 3.1."
        ));
        universityDetailsMap.put("Ural State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Yekaterinburg, Russia",
                "Ural State Medical University (USMU), founded in 1930, is one of the oldest medical schools in the Ural region. USMU offers a 5-year Dentistry program in English, focusing on training qualified dentists. The university is known for its strong clinical base, modern facilities, and commitment to advancing dental education and research. USMU is ranked within the top 1200 universities.",
                "1930",
                "Top 1200",
                "Repina St, 3, Yekaterinburg, Russia",
                "+7 (343) 214-8700",
                "admissions@usmu.edu",
                "High school diploma with relevant science courses.  Entrance test.  TOEFL/IELTS score.  GPA of 3.2."
        ));
        universityDetailsMap.put("Bashkir State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Ufa, Russia",
                "Bashkir State Medical University (BSMU), established in 1932, is a leading medical university in the Republic of Bashkortostan. BSMU offers a 6-year MBBS program in English, providing students with a comprehensive medical education. The university is known for its strong academic programs, clinical training, and research activities. BSMU is ranked within the top 800 universities globally.",
                "1932",
                "Top 800",
                "Lenina St, 3, Ufa, Russia",
                "+7 (347) 273-6100",
                "admissions@bsmu.edu",
                "Secondary school certificate with biology and chemistry.  Entrance exam.  English language proficiency.  GPA of 3.0."
        ));
        universityDetailsMap.put("Omsk State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Omsk, Russia",
                "Omsk State Medical University (OSMU), founded in 1920, is a prominent medical university in Siberia. OSMU offers a 6-year General Medicine program in English, focusing on training physicians. The university is known for its clinical training, research, and contributions to medical science. Omsk State Medical University is ranked within the top 1500.",
                "1920",
                "Top 1500",
                "Lenina St, 12, Omsk, Russia",
                "+7 (3812) 23-3200",
                "admissions@omskmed.edu",
                "High school diploma.  Entrance test in major subjects.  IELTS or equivalent.  GPA of 2.9."
        ));

        // Georgia (7 Universities)
        universityDetailsMap.put("Tbilisi State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "Tbilisi State Medical University (TSMU), with a history dating back to 1918, is a leading medical university in Georgia, providing European-standard medical education. TSMU offers a 6-year MD program in English, attracting international students. The university is known for its strong academic programs, experienced faculty, and modern facilities. TSMU is ranked within the top 500 universities globally.",
                "1918",
                "Top 500",
                "Vazha-Pshavela Ave, 33, Tbilisi, Georgia",
                "+995 (32) 254-2450",
                "admissions@tsmu.edu",
                "Secondary education certificate.  Pass the university's entrance exams.  Provide evidence of English language proficiency.  Minimum GPA of 3.0."
        ));
        universityDetailsMap.put("Batumi Shota Rustaveli State University", new UniversityDetail(
                R.drawable.flag_china,
                "Batumi, Georgia",
                "Batumi Shota Rustaveli State University (BSU), established in 1935, is a public university located in Batumi, Georgia. BSU offers a 6-year MBBS program in English, providing students with a comprehensive medical education. The university is known for its beautiful coastal location, diverse academic programs, and commitment to providing quality education. BSU is ranked within the top 700 universities.",
                "1935",
                "Top 700",
                "Ninoshvili St, 35, Batumi, Georgia",
                "+995 (422) 27-1780",
                "admissions@bsu.edu",
                "High school diploma or equivalent.  Satisfactory results in entrance examinations.  Proof of English language proficiency (IELTS/TOEFL).  GPA of 3.0 or higher."
        ));
        universityDetailsMap.put("David Tvildiani Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "David Tvildiani Medical University (DTMU), founded in 1989, is a private higher education institution focused on medical education. DTMU offers a 6-year General Medicine program in English, emphasizing a problem-based learning approach. The university is known for its innovative teaching methods, modern facilities, and commitment to preparing competent medical professionals. DTMU is ranked within the top 600.",
                "1989",
                "Top 600",
                "Lublin St, 2a, Tbilisi, Georgia",
                "+995 (32) 251-6898",
                "admissions@dtmu.edu",
                "Completed secondary education.  Successful completion of the university’s admission test.  Evidence of English language competence.  GPA of 3.2."
        ));
        universityDetailsMap.put("Caucasus International University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "Caucasus International University (CIU), established in 1995, is a private university in Tbilisi, Georgia. CIU offers a 5-year Dentistry program in English, focusing on training skilled dentists. The university is known for its modern facilities, international collaborations, and commitment to providing quality dental education. CIU is ranked within the top 1000 universities.",
                "1995",
                "Top 1000",
                "Chargali St, 73, Tbilisi, Georgia",
                "+995 (32) 261-1298",
                "admissions@ciu.edu",
                "High school diploma.  Pass the university's entrance exams.  Provide evidence of English language proficiency.  Minimum GPA of 3.0."
        ));
        universityDetailsMap.put("Georgian National University SEU", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "Georgian National University SEU, founded in 2001, is a private university in Tbilisi, Georgia. SEU offers a 6-year MBBS program in English, focusing on training future doctors. The university is known for its modern campus, student-centered approach, and commitment to academic excellence. SEU is ranked within the top 800 universities.",
                "2001",
                "Top 800",
                "Tsinandali St, 9, Tbilisi, Georgia",
                "+995 (32) 290-0000",
                "admissions@seu.edu",
                "Secondary education certificate.  Successful completion of the university’s admission test.  Evidence of English language competence.  GPA of 3.0."
        ));
        universityDetailsMap.put("Akaki Tsereteli State University", new UniversityDetail(
                R.drawable.flag_china,
                "Kutaisi, Georgia",
                "Akaki Tsereteli State University (ATSU), established in 1930, is a public university located in Kutaisi, Georgia. ATSU offers a 6-year General Medicine program in English. The university is one of the oldest educational institutions in Georgia. ATSU is ranked within the top 1200 universities.",
                "1930",
                "Top 1200",
                "Tamar Mepe St, 59, Kutaisi, Georgia",
                "+995 (431) 24-3636",
                "admissions@atsu.edu",
                "High School Diploma with good grades.  Must pass entrance exams.  Submit English Proficiency test results.  GPA of 3.0"
        ));
        universityDetailsMap.put("Ivane Javakhishvili Tbilisi State University", new UniversityDetail(
                R.drawable.flag_china,
                "Tbilisi, Georgia",
                "Ivane Javakhishvili Tbilisi State University (TSU), established in 1918, is the oldest and one of the most prestigious universities in Georgia. TSU offers a 5-year Pharmacy program in English. The university is known for its long history, strong academic traditions, and diverse programs. TSU is ranked within the top 400 universities globally.",
                "1918",
                "Top 400",
                "University St, 1, Tbilisi, Georgia",
                "+995 (32) 222-6710",
                "admissions@tsu.edu",
                "Completed high school education.  Achieve the required scores on the university's entrance examinations.  Provide proof of English language proficiency.  Maintain a GPA of 3.1 or higher."
        ));

        // Kazakhstan (8 Universities)
        universityDetailsMap.put("Kazakh National Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Almaty, Kazakhstan",
                "Kazakh National Medical University (KazNMU), established in 1931, is a leading medical university in Kazakhstan. KazNMU offers a 5-year MBBS program in English, focusing on training qualified medical doctors. The university is known for its strong clinical base, experienced faculty, and commitment to advancing medical education in the region. KazNMU is ranked within the top 1000 universities.",
                "1931",
                "Top 1000",
                "Tole Bi St, 94, Almaty, Kazakhstan",
                "+7 (727) 292-0800",
                "admissions@kaznmu.edu",
                "High school diploma with a science background.  Passage of entrance exams.  English language proficiency certificate (IELTS/TOEFL).  GPA of 3.0."
        ));
        universityDetailsMap.put("Astana Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Nur-Sultan, Kazakhstan",
                "Astana Medical University (AMU), founded in 1964, is a public medical university located in Nur-Sultan, the capital of Kazakhstan. AMU offers a 5-year General Medicine program in English. The university is known for its modern facilities, focus on research, and training of medical professionals. AMU is ranked within the top 800 universities.",
                "1964",
                "Top 800",
                "Beibitshilik St, 49a, Nur-Sultan, Kazakhstan",
                "+7 (7172) 53-9400",
                "admissions@amu.edu",
                "Completed secondary education with a strong foundation in science.  Successful completion of the university's entrance examination.  Evidence of English language proficiency.  GPA of 3.0."
        ));
        universityDetailsMap.put("Semey State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Semey, Kazakhstan",
                "Semey State Medical University (SSMU), established in 1953, is one of the oldest medical universities in Kazakhstan. SSMU offers a 5-year Dentistry program in English, focusing on training qualified dentists. The university is known for its contributions to medical education and research in the region. Semey State Medical University is ranked within the top 1200 universities.",
                "1953",
                "Top 1200",
                "Abay St, 103, Semey, Kazakhstan",
                "+7 (7222) 52-2050",
                "admissions@ssmu.edu",
                "High school diploma with a focus on science subjects.  Successful completion of entrance exams.  English language proficiency test (IELTS/TOEFL).  GPA of 3.0 or higher."
        ));
        universityDetailsMap.put("Karaganda State Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Karaganda, Kazakhstan",
                "Karaganda State Medical University (KSMU), founded in 1950, is a public medical university located in Karaganda, Kazakhstan. KSMU offers a 5-year MBBS program in English. The university is known for its strong academic programs, clinical training, and research activities. Karaganda State Medical University is ranked within the top 900 universities.",
                "1950",
                "Top 900",
                "Gogol St, 40, Karaganda, Kazakhstan",
                "+7 (7212) 51-8970",
                "admissions@ksmu.edu",
                "Completed secondary education.  Passage of university entrance examinations.  Evidence of English language skills.  GPA of 3.1."
        ));
        universityDetailsMap.put("South Kazakhstan Medical Academy", new UniversityDetail(
                R.drawable.flag_china,
                "Shymkent, Kazakhstan",
                "South Kazakhstan Medical Academy (SKMA), established in 1979, is a public medical university located in Shymkent, Kazakhstan. SKMA offers a 5-year Pharmacy program in English. The university is known for its focus on pharmaceutical education and research. South Kazakhstan Medical Academy is ranked within the top 1500 universities.",
                "1979",
                "Top 1500",
                "Al-Farabi Sq, 1, Shymkent, Kazakhstan",
                "+7 (7252) 40-8200",
                "admissions@skma.edu",
                "High school certificate with good grades in sciences.  Successful interview performance.  IELTS or TOEFL score.  GPA of 3.0."
        ));
        universityDetailsMap.put("West Kazakhstan Marat Ospanov Medical University", new UniversityDetail(
                R.drawable.flag_china,
                "Aktobe, Kazakhstan",
                "West Kazakhstan Marat Ospanov Medical University (WKSMU), established in 1957, is a public medical university in Aktobe, Kazakhstan. WKSMU offers a 5-year General Medicine program in English. The university is known for its focus on training medical professionals for the western regions of Kazakhstan. West Kazakhstan Marat Ospanov Medical University is ranked within the top 1100.",
                "1957",
                "Top 1100",
                "Maresyev St, 68, Aktobe, Kazakhstan",
                "+7 (7132) 56-3400",
                "admissions@wkmu.edu",
                "Secondary school diploma with specialization in sciences.  University entrance test.  TOEFL/IELTS scores.  GPA of 3.3."
        ));
        universityDetailsMap.put("Al-Farabi Kazakh National University", new UniversityDetail(
                R.drawable.flag_china,
                "Almaty, Kazakhstan",
                "Al-Farabi Kazakh National University, established in 1934, is a comprehensive university in Almaty, Kazakhstan. It offers a 5-year Pediatrics program in English. The university is known for its diverse programs, research activities, and strong academic reputation. Al-Farabi Kazakh National University is ranked within the top 600 universities globally.",
                "1934",
                "Top 600",
                "Al-Farabi Ave, 71, Almaty, Kazakhstan",
                "+7 (727) 377-3330",
                "admissions@al-farabi.edu",
                "High school diploma.  Entrance test in major subjects.  IELTS or equivalent.  GPA of 2.9."
        ));
        universityDetailsMap.put("Kokshetau State University", new UniversityDetail(
                R.drawable.flag_china,
                "Kokshetau, Kazakhstan",
                "Kokshetau State University, founded in 1996, is a regional university in Kokshetau, Kazakhstan. The university offers a 5-year MBBS program in English. Kokshetau State University is ranked within the top 2000 universities.",
                "1996",
                "Top 2000",
                "Abay St, 76, Kokshetau, Kazakhstan",
                "+7 (7162) 25-6115",
                "admissions@koksu.edu.kz",
                "Secondary education.  Entrance exams.  English language proficiency proof. GPA of 2.8."
        ));
    }

    /**
     * Retrieves the university details based on the university name.
     *
     * @param universityName The name of the university.
     * @return The UniversityDetail object, or null if not found.
     */
    public static UniversityDetail getUniversityDetails(String universityName) {
        return universityDetailsMap.get(universityName);
    }
}

