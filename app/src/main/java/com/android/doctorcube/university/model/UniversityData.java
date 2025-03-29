package com.android.doctorcube.university.model;

import com.android.doctorcube.R;
import java.util.ArrayList;
import java.util.List;

public class UniversityData {
    public static List<University> getUniversities() {
        List<University> universities = new ArrayList<>();

        // Russia (10 Universities)
        universities.add(new University("ru001", "Sechenov University", "Moscow", "Russia",
                "General Medicine", "Bachelor", "6 years", "A+", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 500", "Scholarships up to 30% available"));
        universities.add(new University("ru002", "Moscow State University of Medicine and Dentistry", "Moscow", "Russia",
                "Dentistry", "Bachelor", "6 years", "A", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 500", "Scholarships up to 25% available"));
        universities.add(new University("ru003", "Kazan Federal University", "Kazan", "Russia",
                "MBBS", "Bachelor", "6 years", "A", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 600", "Scholarships up to 20% available"));
        universities.add(new University("ru004", "Novosibirsk State Medical University", "Novosibirsk", "Russia",
                "General Medicine", "Bachelor", "6 years", "B+", "Jan-25, Aug-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 800", "Scholarships up to 15% available"));
        universities.add(new University("ru005", "Saint Petersburg State Medical University", "Saint Petersburg", "Russia",
                "Pediatrics", "Bachelor", "6 years", "A", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 700", "Scholarships up to 25% available"));
        universities.add(new University("ru006", "Volgograd State Medical University", "Volgograd", "Russia",
                "Pharmacy", "Bachelor", "5 years", "B+", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 1000", "Scholarships up to 20% available"));
        universities.add(new University("ru007", "Rostov State Medical University", "Rostov-on-Don", "Russia",
                "General Medicine", "Bachelor", "6 years", "A", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 900", "Scholarships up to 30% available"));
        universities.add(new University("ru008", "Ural State Medical University", "Yekaterinburg", "Russia",
                "Dentistry", "Bachelor", "6 years", "B+", "Jan-25, Aug-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 1200", "Scholarships up to 15% available"));
        universities.add(new University("ru009", "Bashkir State Medical University", "Ufa", "Russia",
                "MBBS", "Bachelor", "6 years", "A", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 800", "Scholarships up to 25% available"));
        universities.add(new University("ru010", "Omsk State Medical University", "Omsk", "Russia",
                "General Medicine", "Bachelor", "6 years", "B+", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 1500", "Scholarships up to 20% available"));

        // Georgia (7 Universities)
        universities.add(new University("ge001", "Tbilisi State Medical University", "Tbilisi", "Georgia",
                "MD", "Bachelor", "6 years", "A", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 500", "Scholarships up to 25% available"));
        universities.add(new University("ge002", "Batumi Shota Rustaveli State University", "Batumi", "Georgia",
                "MBBS", "Bachelor", "6 years", "A", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 700", "Scholarships up to 20% available"));
        universities.add(new University("ge003", "David Tvildiani Medical University", "Tbilisi", "Georgia",
                "General Medicine", "Bachelor", "6 years", "A+", "Mar-25, Oct-25", "English", "Private",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 600", "Scholarships up to 30% available"));
        universities.add(new University("ge004", "Caucasus International University", "Tbilisi", "Georgia",
                "Dentistry", "Bachelor", "6 years", "B+", "Jan-25, Aug-25", "English", "Private",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 1000", "Scholarships up to 15% available"));
        universities.add(new University("ge005", "Georgian National University SEU", "Tbilisi", "Georgia",
                "MBBS", "Bachelor", "6 years", "A", "Feb-25, Sep-25", "English", "Private",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 800", "Scholarships up to 25% available"));
        universities.add(new University("ge006", "Akaki Tsereteli State University", "Kutaisi", "Georgia",
                "General Medicine", "Bachelor", "6 years", "B+", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 1200", "Scholarships up to 20% available"));
        universities.add(new University("ge007", "Ivane Javakhishvili Tbilisi State University", "Tbilisi", "Georgia",
                "Pharmacy", "Bachelor", "5 years", "A", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_georgia,
                "medical", "Top 400", "Scholarships up to 30% available"));

        // Kazakhstan (8 Universities)
        universities.add(new University("kz001", "Kazakh National Medical University", "Almaty", "Kazakhstan",
                "MBBS", "Bachelor", "5 years", "A", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.logo_west_kazakshthan_marat_university, R.drawable.flag_kazakhstan,
                "medical", "Top 1000", "Scholarships up to 50% available"));
        universities.add(new University("kz002", "Astana Medical University", "Nur-Sultan", "Kazakhstan",
                "General Medicine", "Bachelor", "5 years", "A+", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 800", "Scholarships up to 40% available"));
        universities.add(new University("kz003", "Semey State Medical University", "Semey", "Kazakhstan",
                "Dentistry", "Bachelor", "5 years", "B+", "Jan-25, Aug-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 1200", "Scholarships up to 30% available"));
        universities.add(new University("kz004", "Karaganda State Medical University", "Karaganda", "Kazakhstan",
                "MBBS", "Bachelor", "5 years", "A", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 900", "Scholarships up to 35% available"));
        universities.add(new University("kz005", "South Kazakhstan Medical Academy", "Shymkent", "Kazakhstan",
                "Pharmacy", "Bachelor", "5 years", "B+", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 1500", "Scholarships up to 25% available"));
        universities.add(new University("kz006", "West Kazakhstan Marat Ospanov Medical University", "Aktobe", "Kazakhstan",
                "General Medicine", "Bachelor", "5 years", "A", "Jan-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 1100", "Scholarships up to 30% available"));
        universities.add(new University("kz007", "Al-Farabi Kazakh National University", "Almaty", "Kazakhstan",
                "Pediatrics", "Bachelor", "5 years", "A+", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 600", "Scholarships up to 40% available"));
        universities.add(new University("kz008", "Kokshetau State University", "Kokshetau", "Kazakhstan",
                "MBBS", "Bachelor", "5 years", "B+", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_kazakhstan,
                "medical", "Top 2000", "Scholarships up to 20% available"));

        // Nepal (6 Universities)
        universities.add(new University("np001", "Kathmandu Medical College", "Kathmandu", "Nepal",
                "MBBS", "Bachelor", "5.5 years", "A", "Jan-25, Aug-25", "English", "Private",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_nepal,
                "medical", "Top 1500", "Scholarships up to 20% available"));
        universities.add(new University("np002", "B.P. Koirala Institute of Health Sciences", "Dharan", "Nepal",
                "General Medicine", "Bachelor", "5.5 years", "A+", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_nepal,
                "medical", "Top 1000", "Scholarships up to 25% available"));
        universities.add(new University("np003", "Tribhuvan University Institute of Medicine", "Kathmandu", "Nepal",
                "MBBS", "Bachelor", "5.5 years", "A", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_nepal,
                "medical", "Top 800", "Scholarships up to 30% available"));
        universities.add(new University("np004", "Nepal Medical College", "Kathmandu", "Nepal",
                "Dentistry", "Bachelor", "5.5 years", "B+", "Jan-25, Aug-25", "English", "Private",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_nepal,
                "medical", "Top 2000", "Scholarships up to 15% available"));
        universities.add(new University("np005", "Manipal College of Medical Sciences", "Pokhara", "Nepal",
                "MBBS", "Bachelor", "5.5 years", "A", "Feb-25, Sep-25", "English", "Private",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_nepal,
                "medical", "Top 1200", "Scholarships up to 25% available"));
        universities.add(new University("np006", "Patan Academy of Health Sciences", "Lalitpur", "Nepal",
                "General Medicine", "Bachelor", "5.5 years", "A+", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_nepal,
                "medical", "Top 900", "Scholarships up to 20% available"));

        // China (10 Universities)
        universities.add(new University("ch001", "Peking University", "Beijing", "China",
                "MBBS", "Bachelor", "5 years", "A+", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 100", "Scholarships up to 40% available"));
        universities.add(new University("ch002", "Fudan University", "Shanghai", "China",
                "MBBS", "Bachelor", "6 years", "A+", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 50", "Scholarships up to 50% available"));
        universities.add(new University("ch003", "Zhejiang University", "Hangzhou", "China",
                "General Medicine", "Bachelor", "6 years", "A", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 200", "Scholarships up to 35% available"));
        universities.add(new University("ch004", "Shanghai Jiao Tong University", "Shanghai", "China",
                "Dentistry", "Bachelor", "6 years", "A+", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 150", "Scholarships up to 40% available"));
        universities.add(new University("ch005", "Sun Yat-sen University", "Guangzhou", "China",
                "MBBS", "Bachelor", "6 years", "A", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 300", "Scholarships up to 30% available"));
        universities.add(new University("ch006", "Nanjing Medical University", "Nanjing", "China",
                "Pharmacy", "Bachelor", "5 years", "A", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 400", "Scholarships up to 25% available"));
        universities.add(new University("ch007", "Tongji University", "Shanghai", "China",
                "General Medicine", "Bachelor", "6 years", "A+", "Jan-25, Aug-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 250", "Scholarships up to 35% available"));
        universities.add(new University("ch008", "China Medical University", "Shenyang", "China",
                "MBBS", "Bachelor", "6 years", "A", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 500", "Scholarships up to 30% available"));
        universities.add(new University("ch009", "Huazhong University of Science and Technology", "Wuhan", "China",
                "Pediatrics", "Bachelor", "6 years", "A+", "Mar-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 200", "Scholarships up to 40% available"));
        universities.add(new University("ch010", "Sichuan University", "Chengdu", "China",
                "Dentistry", "Bachelor", "6 years", "A", "Feb-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 350", "Scholarships up to 35% available"));

        // Uzbekistan (6 Universities)
        universities.add(new University("uz001", "Samarkand State Medical Institute", "Samarkand", "Uzbekistan",
                "General Medicine", "Bachelor", "6 years", "B+", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_uzbekistan,
                "medical", "Top 2000", "Scholarships up to 30% available"));
        universities.add(new University("uz002", "Tashkent Medical Academy", "Tashkent", "Uzbekistan",
                "MBBS", "Bachelor", "6 years", "A", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_uzbekistan,
                "medical", "Top 1500", "Scholarships up to 35% available"));
        universities.add(new University("uz003", "Bukhara State Medical Institute", "Bukhara", "Uzbekistan",
                "Dentistry", "Bachelor", "6 years", "B+", "Jan-25, Aug-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_uzbekistan,
                "medical", "Top 2500", "Scholarships up to 25% available"));
        universities.add(new University("uz004", "Andijan State Medical Institute", "Andijan", "Uzbekistan",
                "General Medicine", "Bachelor", "6 years", "A", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_uzbekistan,
                "medical", "Top 1800", "Scholarships up to 30% available"));
        universities.add(new University("uz005", "Fergana State University Medical Faculty", "Fergana", "Uzbekistan",
                "MBBS", "Bachelor", "6 years", "B+", "Mar-25, Sep-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_uzbekistan,
                "medical", "Top 2200", "Scholarships up to 20% available"));
        universities.add(new University("uz006", "Urgench Branch of Tashkent Medical Academy", "Urgench", "Uzbekistan",
                "Pharmacy", "Bachelor", "5 years", "A", "Feb-25, Oct-25", "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_uzbekistan,
                "medical", "Top 2000", "Scholarships up to 25% available"));

        return universities;
    }
}