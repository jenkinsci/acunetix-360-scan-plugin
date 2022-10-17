package com.acunetix.model;

public enum ReportType {
	ScanDetail(3), 
	OwaspTopTen2013(5), 
	HIPAACompliance(6), 
	PCICompliance(7),
	KnowledgeBase(8),
	ExecutiveSummary(9),
	OwaspTopTen2017(10),
	SansTop25(11),
	WASC(12),
	Iso27001Compliance(13),
	FullScanDetail(14);

	private final int number;

	ReportType(final int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public String getNumberAsString() {
		return String.valueOf(number);
	}
}
