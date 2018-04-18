package cn.com.zach.tools.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * base62编解码, 例如: //编码 String code= Base62.encode(Long.MAX_VALUE);
 * System.out.println(code);
 * 
 * //解码 Long l = Base62.decode(code); System.out.println(Long.MAX_VALUE +" "+ l
 * );
 * 
 * 
 * 
 * @author zach
 */
public class Base62 {
	// 5位编码长度数值接近12.52亿
	public static final char[] STANDARD_ALPHABET = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z',
			/* '-','_','!','.','~',')','(','*','~', */
	};

	public static final char[] DISORDER_ALPHABET =
			// 生成随机的字符串,防止猜测
			{ 'C', 't', 'N', 'O', 'S', 'T', 'M', 'i', 'Q', 'J', 'D', 'A', '1', 'W', 'I', 'e', '0', 'v', 's', 'j', '2',
					'o', 'G', 'w', 'B', 'a', 'Y', 'F', '9', 'd', 'L', 'p', '3', 'Z', 'y', 'H', 'q', 'c', 'P', '5', 'K',
					'g', 'U', 'R', 'z', '8', 'u', 'k', 'E', '6', 'V', 'x', 'n', 'm', 'X', 'h', '4', 'b', '7', 'r', 'f',
					'l', };

	// 寻找一个素数
	private static final long PRIME = 2147483647l;

	public static final int BASE = STANDARD_ALPHABET.length;

	private static final Map<Character, Integer> DISORDER_MAP = new ConcurrentHashMap<Character, Integer>(DISORDER_ALPHABET.length);

	private static final Map<Character, Integer> STANDARD_MAP = new ConcurrentHashMap<Character, Integer>(STANDARD_ALPHABET.length);

	static {
		for (int index = 0; index < 62; index++) {
			STANDARD_MAP.put(STANDARD_ALPHABET[index], index);
		}
		for (int index = 0; index < 62; index++) {
			DISORDER_MAP.put(DISORDER_ALPHABET[index], index);
		}
	}

	/**
	 * 乱序编码
	 * 
	 * @param num
	 * @return
	 */
	public static String enDisord(long b10) {
		b10 = b10 ^ PRIME;
		String answer = "";
		while (b10 > 0) {
			answer = DISORDER_ALPHABET[(int) (b10 % 62)] + answer;
			b10 /= 62;
		}
		return answer;
	}

	/**
	 * 有序编码
	 * 
	 * @param num
	 * @return
	 */
	public static String encode(long b10) {
		String answer = "";
		while (b10 > 0) {
			answer = STANDARD_ALPHABET[(int) (b10 % 62)] + answer;
			b10 /= 62;
		}
		return answer;
	}

	/**
	 * 有序解码
	 * 
	 * @param str
	 * @return
	 */
	public static long decode(String str) {
		long num = 0;
		for (int i = 0, len = str.length(); i < len; i++) {
			num = num * BASE + STANDARD_MAP.get(str.charAt(i));
		}
		return num;
	}

	/**
	 * 乱序解码
	 * 
	 * @param str
	 * @return
	 */
	public static long deDisord(String b62) {
		long num = 0;
		for (int i = 0, len = b62.length(); i < len; i++) {
			num = num * BASE + DISORDER_MAP.get(b62.charAt(i));
		}
		return num ^ PRIME;
	}

}