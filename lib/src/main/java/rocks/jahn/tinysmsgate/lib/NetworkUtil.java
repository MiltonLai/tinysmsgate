package rocks.jahn.tinysmsgate.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;


public class NetworkUtil {
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");
    private static final int TYPE_ALL = -1;
    private static final int TYPE_IPV4 = 0;
    private static final int TYPE_IPV6 = 1;

    private static final Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

    public static List<String> getIpv4Addresses() {
        return getIpAddresses(TYPE_IPV4);
    }

    public static List<String> getIpv6Addresses() {
        return getIpAddresses(TYPE_IPV6);
    }

    public static List<String> getIpAddresses() {
        return getIpAddresses(TYPE_ALL);
    }

    /**
     * Get IP address from non-localhost interface
     *
     * @param type: -1:all, 0:ipv4, 1:ipv6
     */
    private static List<String> getIpAddresses(int type) {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
        List<String> output = new ArrayList<>();
        while (interfaces.hasMoreElements()) {
            NetworkInterface inf = interfaces.nextElement();
            Enumeration<InetAddress> addresses = inf.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress()) {
                    String sAddr = address.getHostAddress();
                    if (isIPv4Address(sAddr)) {
                        if (type != 1) {
                            output.add(sAddr);
                        }
                    } else {
                        if (type != 0) {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            String ipv6Addr = (delim < 0)? sAddr : sAddr.substring(0, delim);
                            output.add(ipv6Addr);
                        }
                    }
                }
            }
        }
        return output;
    }

    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6StdAddress(String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6HexCompressedAddress(String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }
}