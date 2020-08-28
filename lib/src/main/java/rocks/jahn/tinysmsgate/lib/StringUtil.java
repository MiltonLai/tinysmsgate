package rocks.jahn.tinysmsgate.lib;

import java.util.Collection;
import java.util.Iterator;

public class StringUtil {

    public static String implode(Collection<String> collection, String separator) {
        if (collection == null || collection.size() == 0) return "";
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = collection.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());
            while (iterator.hasNext()) {
                sb.append(separator);
                sb.append(iterator.next());
            }
        }
        return sb.toString();
    }
}
