package com.project.baedalsodae.order.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderNoGenerator {

	public static String generate() {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder random = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			random.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
		}
		return date + "-" + random;
	}

}
