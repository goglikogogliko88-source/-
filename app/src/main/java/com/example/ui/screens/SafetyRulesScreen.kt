package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SafetyRulesScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Mandatory Official Legal Disclaimer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningOrange)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(24.dp))
                        Text(
                            text = "ოფიციალური რეგულაციები",
                            color = WarningOrange,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ოფიციალურ წყაროსთან გადამოწმება აუცილებელია",
                        color = AccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "საქართველოში ნადირობის ვადები, კვოტები, ნებადართული იარაღის ტიპები და სახელმწიფო მოსაკრებლები რეგულირდება საქართველოს გარემოს დაცვისა და სოფლის მეურნეობის სამინისტროს მიერ. ყოველი სეზონის წინ გადაამოწმეთ ოფიციალური ბრძანება.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Georgian Hunting Seasons Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "სანადირო სეზონების საორიენტაციო ვადები",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SeasonRowItem(
                        species = "მწყერი (Coturnix coturnix)",
                        period = "აგვისტოს მე-3 შაბათი – 15 თებერვალი",
                        limit = "დღიური ლიმიტი: 20 ცალი",
                        isOpen = true
                    )
                    SeasonRowItem(
                        species = "ქედანი და გარეული მტრედი",
                        period = "აგვისტოს მე-3 შაბათი – 15 თებერვალი",
                        limit = "დღიური ლიმიტი: 10 ცალი",
                        isOpen = true
                    )
                    SeasonRowItem(
                        species = "ტყის ქათამი (Scolopax rusticola)",
                        period = "1 ოქტომბერი – 15 თებერვალი",
                        limit = "დღიური ლიმიტი: 5 ცალი",
                        isOpen = false
                    )
                    SeasonRowItem(
                        species = "წყალმცურავი ფრინველები (იხვი, ბატი)",
                        period = "1 ნოემბერი – 1 მარტი",
                        limit = "დღიური ლიმიტი: 6 ცალი",
                        isOpen = false
                    )
                    SeasonRowItem(
                        species = "გარეული ღორი / ტახი",
                        period = "სპეციალური სალიცენზიო კვოტით სანადირო მეურნეობებში",
                        limit = "მკაცრად კონტროლირებადი",
                        isOpen = true
                    )
                }
            }
        }

        // Firearm 10 Golden Safety Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGold, modifier = Modifier.size(22.dp))
                        Text(
                            text = "იარაღის უსაფრთხოების 10 ოქროს წესი",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    SafetyRuleItem("1", "ყოველთვის მოექეცით იარაღს ისე, თითქოს ის დატენილია.")
                    SafetyRuleItem("2", "ლულა ყოველთვის მიმართეთ მხოლოდ უსაფრთხო მიმართულებით.")
                    SafetyRuleItem("3", "თითი სასხლეტზე დაადეთ მხოლოდ მაშინ, როდესაც მიზანში ამოიღებთ და მზად ხართ სასროლად.")
                    SafetyRuleItem("4", "მკაფიოდ ამოიცანით სამიზნე და დარწმუნდით, რა არის მის უკან.")
                    SafetyRuleItem("5", "არასოდეს ისროლოთ ხმაურზე ან ბუჩქების შრიალზე დაუნახავად.")
                    SafetyRuleItem("6", "დაბრკოლების გადალახვისას (ღობე, თხრილი) აუცილებლად განმუხტეთ იარაღი.")
                    SafetyRuleItem("7", "ტრანსპორტირებისას იარაღი უნდა იყოს დაშლილი ან შალითაში, განმუხტულ მდგომარეობაში.")
                    SafetyRuleItem("8", "ალკოჰოლისა და მედიკამენტების ზემოქმედების ქვეშ იარაღის ხმარება კატეგორიულად აკრძალულია.")
                    SafetyRuleItem("9", "გამოიყენეთ მხოლოდ შესაბამისი ყალიბის და უსაფრთხო ვაზნები.")
                    SafetyRuleItem("10", "იარაღი შეინახეთ სეიფში, ვაზნებისგან განცალკევებით და ბავშვებისთვის მიუწვდომელ ადგილას.")
                }
            }
        }

        // Emergency SOS & Mountain Orientation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Sos, contentDescription = null, tint = AlertRed, modifier = Modifier.size(24.dp))
                        Text(
                            text = "გადაუდებელი დახმარება და SOS",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ForestDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("საქართველოს გადაუდებელი დახმარება:", color = TextSecondary, fontSize = 11.sp)
                                Text("112", color = AlertRed, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                            Text("უფასო ზარი ყველა ოპერატორიდან", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ორიენტაციის რჩევა: მთაში დაკარგვისას იმოძრავეთ წყლის ნაკადის მიმართულებით ქვემოთ ხეობისკენ, სადაც ყოველთვის შეხვდებით გზას ან დასახლებას.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonRowItem(
    species: String,
    period: String,
    limit: String,
    isOpen: Boolean
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(species, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isOpen) HuntingGreenDark else ForestSurfaceVariant
            ) {
                Text(
                    text = if (isOpen) "ღიაა" else "მოლოდინში",
                    color = if (isOpen) HuntingGreenLight else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Text(period, color = AccentGold, fontSize = 11.sp)
        Text(limit, color = TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = ForestCardBorder.copy(alpha = 0.4f))
    }
}

@Composable
private fun SafetyRuleItem(number: String, rule: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = ForestSurfaceVariant,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(rule, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
    }
}
