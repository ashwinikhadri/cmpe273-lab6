package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Client {

	private final static SortedMap<Integer, String> ring = new TreeMap<Integer, String>();
	private static HashFunction hashFunction = Hashing.md5();
	private static ArrayList<String> serverList = new ArrayList<String>();
	static char[] characters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
			'z' };

	public static void main(String[] args) throws Exception {

		serverList.add("http://localhost:3000");
		serverList.add("http://localhost:3001");
		serverList.add("http://localhost:3002");
		for (int i = 0; i < serverList.size(); i++) {

			addServer(serverList.get(i), i);
		}

		for (int j = 0; j < 10; j++) {
			int bucket = Hashing.consistentHash(Hashing.md5().hashInt(j),
					ring.size());
			String server = get(bucket);
			System.out.println("Routed to this Server: " + server);
			CacheServiceInterface cache = new DistributedCacheService(server);
			cache.put(j + 1, String.valueOf(characters[j]));
			String value = cache.get(j + 1);
			System.out.println("get(" + (j + 1) + ") => " + value);

		}

		System.out.println("Exiting Cache Client...");
	}

	public static void addServer(String server, int i) {
		HashCode hc = hashFunction.hashLong(i);
		ring.put(hc.asInt(), server);
	}

	public static void removeServer(int key) {
		int hash = hashFunction.hashLong(key).asInt();
		ring.remove(hash);
	}

	public static String get(Object key) {
		if (ring.isEmpty()) {
			return null;
		}
		int hash = hashFunction.hashLong((Integer) key).asInt();
		if (!ring.containsKey(hash)) {
			SortedMap<Integer, String> tailMap = ring.tailMap(hash);
			hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
		}
		return ring.get(hash);
	}
}