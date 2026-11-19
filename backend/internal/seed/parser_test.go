package seed

import "testing"

func TestParseKHR(t *testing.T) {
	cases := map[string]int64{
		"0 KHR":     0,
		"1,000 KHR": 1000,
		"5,000 KHR": 5000,
	}

	for input, expected := range cases {
		if actual := ParseKHR(input); actual != expected {
			t.Fatalf("ParseKHR(%q) = %d, want %d", input, actual, expected)
		}
	}
}

func TestParseUSD(t *testing.T) {
	cases := map[string]float64{
		"$37.00": 37,
		"$0.00":  0,
		"$1.50":  1.5,
	}

	for input, expected := range cases {
		if actual := ParseUSD(input); actual != expected {
			t.Fatalf("ParseUSD(%q) = %f, want %f", input, actual, expected)
		}
	}
}

func TestParseHours(t *testing.T) {
	ranges, is24 := ParseHours("24 Hours")
	if !is24 || len(ranges) != 0 {
		t.Fatalf("expected 24-hour result, got %+v %v", ranges, is24)
	}

	ranges, is24 = ParseHours("08:00 - 11:00, 14:00 - 17:00")
	if is24 || len(ranges) != 2 {
		t.Fatalf("expected two ranges, got %+v %v", ranges, is24)
	}
	if ranges[0].OpensAt != "8:00:00" || ranges[0].ClosesAt != "11:00:00" {
		t.Fatalf("unexpected first range: %+v", ranges[0])
	}
	if ranges[1].OpensAt != "14:00:00" || ranges[1].ClosesAt != "17:00:00" {
		t.Fatalf("unexpected second range: %+v", ranges[1])
	}
}
