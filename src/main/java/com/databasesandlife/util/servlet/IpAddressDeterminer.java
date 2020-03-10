package com.databasesandlife.util.servlet;

import org.apache.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddressDeterminer {

    public @CheckForNull InetAddress getRequestIpAddress(@Nonnull HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) ipAddress = request.getRemoteAddr();
            return ipAddress == null ? null : InetAddress.getByName(ipAddress);
        }
        catch (UnknownHostException e) {
            Logger.getLogger(getClass()).warn("Unexpected exception when fetching IP address, will ignore: " + e.getMessage());
            return null;
        }
    }
}
