package auction.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static <K, V> Map<V, K> reverse(Map<K, V> map) {
        Map<V, K> reversedMap = new HashMap<>();
        for (K k : map.keySet()) {
            reversedMap.put(map.get(k), k);
        }
		return reversedMap;
    }
}
