package com.example.domain

import com.example.data.model.OfficerSpecialty

data class RecruitTemplate(
    val id: String,
    val name: String,
    val specialty: OfficerSpecialty,
    val basePower: Int,
    val hireCost: Long,
    val unlockLevel: Int,
    val bioFa: String
)

data class VehicleTemplate(
    val modelKey: String,
    val name: String,
    val subtitleFa: String,
    val baseSpeedBonus: Int,
    val basePowerBonus: Int,
    val cost: Long,
    val unlockLevel: Int
)

data class LevelMilestone(
    val level: Int,
    val rankTitleFa: String,
    val xpRequired: Int,
    val unlockDescriptionFa: String
)

object GameCatalog {
    const val MAX_PLAYER_LEVEL = 20
    const val MAX_OFFICER_LEVEL = 15
    const val MAX_VEHICLE_LEVEL = 10
    const val MAX_BUILDING_LEVEL = 10
    const val MAX_CAPACITY_LEVEL = 10
    const val MAX_PRISON_UPGRADE_LEVEL = 10

    val criminalProfiles: List<Pair<String, String>> = listOf(
        "فریدون ملکی" to "سایه شب",
        "بهزاد کریمیان" to "قفل‌شکن",
        "کامران تهرانی" to "توربو",
        "نادر رستمی" to "شبح بازار",
        "سیامک نادری" to "دست‌چابک",
        "مهران شکری" to "روباه خیابان",
        "پویا فرهادی" to "زبل",
        "شهرام کاظمی" to "ناپدید",
        "کاوه جمشیدی" to "کلیدساز",
        "تورج شریفی" to "بزرگراه",
        "سعید بهرامی" to "کلاف",
        "رامین پارسا" to "شب‌رو"
    )

    val districts = listOf(
        "بانک مرکزی" to (0.20f to 0.24f),
        "جواهری مرکزی" to (0.50f to 0.24f),
        "مغازه‌ها و بازار" to (0.20f to 0.50f),
        "پارک شهر" to (0.24f to 0.74f),
        "ماشین‌فروشی" to (0.80f to 0.68f),
        "بزرگراه شرقی" to (0.78f to 0.44f),
        "مرکز شهر" to (0.64f to 0.36f),
        "اسکله ساحلی" to (0.56f to 0.80f)
    )

    val recruitTemplates: List<RecruitTemplate> = listOf(
        RecruitTemplate(
            id = "rec_patrol_1",
            name = "استوار نیما رضایی",
            specialty = OfficerSpecialty.PATROL,
            basePower = 26,
            hireCost = 220L,
            unlockLevel = 1,
            bioFa = "مأمور پرانرژی گشت محله با واکنش سریع"
        ),
        RecruitTemplate(
            id = "rec_patrol_2",
            name = "گروهبان مریم احمدی",
            specialty = OfficerSpecialty.PATROL,
            basePower = 30,
            hireCost = 310L,
            unlockLevel = 1,
            bioFa = "مسلط به آرام‌سازی سریع موقعیت‌های شهری"
        ),
        RecruitTemplate(
            id = "rec_traffic_1",
            name = "ستوان امیرحسین شریفی",
            specialty = OfficerSpecialty.TRAFFIC,
            basePower = 36,
            hireCost = 430L,
            unlockLevel = 2,
            bioFa = "متخصص کنترل ترافیک و مدیریت تصادفات"
        ),
        RecruitTemplate(
            id = "rec_detective_1",
            name = "کارآگاه نازنین کاظمی",
            specialty = OfficerSpecialty.DETECTIVE,
            basePower = 44,
            hireCost = 620L,
            unlockLevel = 3,
            bioFa = "تیزبین در سرنخ‌یابی و یافتن افراد گمشده"
        ),
        RecruitTemplate(
            id = "rec_patrol_3",
            name = "سروان بهرام مرادی",
            specialty = OfficerSpecialty.PATROL,
            basePower = 52,
            hireCost = 850L,
            unlockLevel = 5,
            bioFa = "فرمانده باتجربه گشت‌های شبانه"
        ),
        RecruitTemplate(
            id = "rec_traffic_2",
            name = "سروان لیلا جعفری",
            specialty = OfficerSpecialty.TRAFFIC,
            basePower = 60,
            hireCost = 1100L,
            unlockLevel = 6,
            bioFa = "متخصص ردیابی و کشف خودروهای مسروقه"
        ),
        RecruitTemplate(
            id = "rec_tactical_1",
            name = "سرگرد کیانوش پارسایی",
            specialty = OfficerSpecialty.TACTICAL,
            basePower = 72,
            hireCost = 1500L,
            unlockLevel = 8,
            bioFa = "متخصص تعقیب ایمن و مهار مجرمان فراری"
        ),
        RecruitTemplate(
            id = "rec_detective_2",
            name = "سرگرد فرهاد نوری",
            specialty = OfficerSpecialty.DETECTIVE,
            basePower = 82,
            hireCost = 1950L,
            unlockLevel = 10,
            bioFa = "کارشناس ارشد پرونده‌های پیچیده شهری"
        ),
        RecruitTemplate(
            id = "rec_negotiator_1",
            name = "سرگرد الهام صادقی",
            specialty = OfficerSpecialty.NEGOTIATOR,
            basePower = 94,
            hireCost = 2500L,
            unlockLevel = 11,
            bioFa = "مذاکره‌کننده حرفه‌ای برای حل مسالمت‌آمیز بحران‌ها"
        ),
        RecruitTemplate(
            id = "rec_tactical_2",
            name = "سرهنگ دوم سهراب حیدری",
            specialty = OfficerSpecialty.TACTICAL,
            basePower = 110,
            hireCost = 3400L,
            unlockLevel = 14,
            bioFa = "فرمانده تیم واکنش سریع در عملیات‌های کلان"
        ),
        RecruitTemplate(
            id = "rec_negotiator_2",
            name = "سرهنگ دوم پرستو ملکی",
            specialty = OfficerSpecialty.NEGOTIATOR,
            basePower = 124,
            hireCost = 4300L,
            unlockLevel = 16,
            bioFa = "متخصص ارشد مدیریت بحران و حفظ امنیت شهروندان"
        ),
        RecruitTemplate(
            id = "rec_elite_1",
            name = "سرهنگ داریوش سپهری",
            specialty = OfficerSpecialty.TACTICAL,
            basePower = 145,
            hireCost = 5800L,
            unlockLevel = 18,
            bioFa = "افسر افسانه‌ای عملیات هماهنگ امنیت شهر"
        )
    )

    val vehicleTemplates: List<VehicleTemplate> = listOf(
        VehicleTemplate(
            modelKey = "moto_patrol",
            name = "موتور گشت چابک",
            subtitleFa = "ورود سریع به کوچه‌ها و کاهش زمان مأموریت",
            baseSpeedBonus = 18,
            basePowerBonus = 8,
            cost = 360L,
            unlockLevel = 1
        ),
        VehicleTemplate(
            modelKey = "sedan_cruiser",
            name = "سدان گشت کلانتری",
            subtitleFa = "خودروی استاندارد و مطمئن گشت‌زنی شهری",
            baseSpeedBonus = 12,
            basePowerBonus = 14,
            cost = 580L,
            unlockLevel = 1
        ),
        VehicleTemplate(
            modelKey = "traffic_van",
            name = "خودروی پلیس راهور",
            subtitleFa = "مجهز به چراغ‌گردان هوشمند و تجهیزات راهور",
            baseSpeedBonus = 16,
            basePowerBonus = 22,
            cost = 1050L,
            unlockLevel = 3
        ),
        VehicleTemplate(
            modelKey = "suv_patrol",
            name = "شاسی‌بلند گشت شهری",
            subtitleFa = "قدرت بالا برای انتقال چند مأمور به نقاط دشوار",
            baseSpeedBonus = 20,
            basePowerBonus = 34,
            cost = 1850L,
            unlockLevel = 5
        ),
        VehicleTemplate(
            modelKey = "turbo_interceptor",
            name = "خودروی تعقیب توربو",
            subtitleFa = "سرعت فوق‌العاده در بزرگراه‌ها و عملیات تعقیب",
            baseSpeedBonus = 28,
            basePowerBonus = 46,
            cost = 3100L,
            unlockLevel = 8
        ),
        VehicleTemplate(
            modelKey = "mobile_hq",
            name = "ون فرماندهی سیار",
            subtitleFa = "مجهز به رادار میدانی و مرکز هماهنگی بحران",
            baseSpeedBonus = 22,
            basePowerBonus = 68,
            cost = 5200L,
            unlockLevel = 12
        ),
        VehicleTemplate(
            modelKey = "police_heli",
            name = "بالگرد گشت هوایی",
            subtitleFa = "پشتیبانی هوایی سریع و پوشش سراسری نقشه شهر",
            baseSpeedBonus = 38,
            basePowerBonus = 92,
            cost = 8900L,
            unlockLevel = 16
        )
    )

    val milestones: List<LevelMilestone> = listOf(
        LevelMilestone(1, "ستوان تازه‌کار", 100, "شروع کار کلانتری، مأموریت سرقت فروشگاه و ایجاد مزاحمت"),
        LevelMilestone(2, "ستوان دوم شهر", 165, "باز شدن مأموریت تصادف رانندگی و پلیس راهور"),
        LevelMilestone(3, "ستوان یکم گشت", 240, "باز شدن مأموریت گم شدن افراد، کارآگاه و خودروی راهور"),
        LevelMilestone(4, "سروان محله", 330, "باز شدن مأموریت درگیری خیابانی و افزایش ظرفیت کلانتری"),
        LevelMilestone(5, "سروان ارشد", 435, "باز شدن شاسی‌بلند گشت و فرمانده گشت شبانه"),
        LevelMilestone(6, "سرگرد تازه‌کار", 560, "باز شدن مأموریت سرقت خودرو و سروان ردیابی"),
        LevelMilestone(7, "سرگرد امنیت", 700, "افزایش پاداش روزانه کلانتری و مأموریت‌های سطح بالاتر"),
        LevelMilestone(8, "سرگرد عملیات", 860, "باز شدن تعقیب مجرم، افسر عملیات ویژه و خودروی توربو"),
        LevelMilestone(9, "معاون کلانتری", 1040, "افزایش اعتبار شهر و بودجه تشویقی شیفت"),
        LevelMilestone(10, "فرمانده کلانتری", 1240, "باز شدن کارآگاه ارشد و ارتقای رده کلانتری"),
        LevelMilestone(11, "سرهنگ دوم", 1460, "باز شدن مأموریت نجات گروگان و تیم مذاکره‌کننده"),
        LevelMilestone(12, "سرهنگ دوم ارشد", 1700, "باز شدن ون فرماندهی سیار و افزایش ظرفیت نیرو"),
        LevelMilestone(13, "سرهنگ منطقه", 1960, "تقویت پاداش عملیات‌های حساس شهری"),
        LevelMilestone(14, "سرهنگ تمام", 2240, "باز شدن مأموریت طلافروشی و فرمانده واکنش سریع"),
        LevelMilestone(15, "فرمانده ناحیه", 2540, "دسترسی به عملیات‌های پرپاداش کلان‌شهر"),
        LevelMilestone(16, "سرتیپ دوم", 2860, "باز شدن بالگرد گشت هوایی و مهار بحران بزرگراه"),
        LevelMilestone(17, "سرتیپ امنیت شهر", 3200, "افزایش حداکثر ظرفیت عملیاتی کلانتری"),
        LevelMilestone(18, "فرمانده کل عملیات", 3580, "باز شدن عملیات امنیت شهر و افسر افسانه‌ای"),
        LevelMilestone(19, "رئیس ارشد پلیس", 4000, "آماده‌سازی برای کسب بالاترین نشان افتخار شهر"),
        LevelMilestone(20, "رئیس پلیس افسانه‌ای", 0, "بالاترین رتبه مدیریت امنیت کلان‌شهر!")
    )

    fun milestoneFor(level: Int): LevelMilestone {
        val clamped = level.coerceIn(1, MAX_PLAYER_LEVEL)
        return milestones[clamped - 1]
    }

    fun xpRequiredForLevel(level: Int): Int = milestoneFor(level).xpRequired

    fun rankTitleForLevel(level: Int): String = milestoneFor(level).rankTitleFa
}
