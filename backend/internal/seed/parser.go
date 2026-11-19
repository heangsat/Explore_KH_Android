package seed

import (
	"regexp"
	"strconv"
	"strings"
)

type File struct {
	Provinces   []ProvinceRecord   `json:"provinces"`
	Attractions []AttractionRecord `json:"attractions"`
}

type ProvinceRecord struct {
	ID          string `json:"id"`
	NameEn      string `json:"nameEn"`
	NameKh      string `json:"nameKh"`
	Region      string `json:"region"`
	Description string `json:"description"`
	ImageURL    string `json:"imageUrl"`
}

type AttractionRecord struct {
	ID            string  `json:"id"`
	ProvinceID    string  `json:"provinceId"`
	TitleEn       string  `json:"titleEn"`
	TitleKh       string  `json:"titleKh"`
	Category      string  `json:"category"`
	DescriptionEn string  `json:"descriptionEn"`
	DescriptionKh string  `json:"descriptionKh"`
	Latitude      float64 `json:"latitude"`
	Longitude     float64 `json:"longitude"`
	PriceKhr      string  `json:"priceKhr"`
	PriceUsd      string  `json:"priceUsd"`
	OpeningHours  string  `json:"openingHours"`
	Rating        float64 `json:"rating"`
	ImageURL      string  `json:"imageUrl"`
}

type HourRange struct {
	OpensAt  string
	ClosesAt string
}

var (
	hourRangePattern = regexp.MustCompile(`(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})`)
	digitsPattern    = regexp.MustCompile(`[0-9]+`)
)

func ParseKHR(value string) int64 {
	digits := strings.Join(digitsPattern.FindAllString(value, -1), "")
	if digits == "" {
		return 0
	}
	parsed, err := strconv.ParseInt(digits, 10, 64)
	if err != nil {
		return 0
	}
	return parsed
}

func ParseUSD(value string) float64 {
	cleaned := strings.NewReplacer("$", "", ",", "", " ", "").Replace(value)
	if cleaned == "" {
		return 0
	}
	parsed, err := strconv.ParseFloat(cleaned, 64)
	if err != nil {
		return 0
	}
	return parsed
}

// ParseHours converts the spreadsheet's display string into concrete ranges.
// Examples: "05:00 - 17:30", "08:00 - 11:00, 14:00 - 17:00", "24 Hours".
func ParseHours(value string) ([]HourRange, bool) {
	trimmed := strings.TrimSpace(value)
	if strings.EqualFold(trimmed, "24 Hours") {
		return nil, true
	}

	matches := hourRangePattern.FindAllStringSubmatch(trimmed, -1)
	ranges := make([]HourRange, 0, len(matches))
	for _, match := range matches {
		openHour, openMinute := parseClock(match[1], match[2])
		closeHour, closeMinute := parseClock(match[3], match[4])
		if openHour < 0 || closeHour < 0 {
			continue
		}
		ranges = append(ranges, HourRange{
			OpensAt:  clockString(openHour, openMinute),
			ClosesAt: clockString(closeHour, closeMinute),
		})
	}

	return ranges, false
}

func parseClock(hourText, minuteText string) (int, int) {
	hour, err := strconv.Atoi(hourText)
	if err != nil || hour < 0 || hour > 23 {
		return -1, -1
	}
	minute, err := strconv.Atoi(minuteText)
	if err != nil || minute < 0 || minute > 59 {
		return -1, -1
	}
	return hour, minute
}

func clockString(hour, minute int) string {
	return strconv.Itoa(hour) + ":" + pad2(minute) + ":00"
}

func pad2(value int) string {
	if value < 10 {
		return "0" + strconv.Itoa(value)
	}
	return strconv.Itoa(value)
}
