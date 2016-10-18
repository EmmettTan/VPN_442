package Model;

import Helper.Common;
import Ui.VpnUi;

import java.awt.*;
import java.io.IOException;

/**
 * Created by karui on 2016-10-03.
 */
public class Vpn {
    private static VpnManager vpnManager;
    private static VpnUi vpnUi;

    private Vpn() {
        vpnUi = VpnUi.getInstance();
        vpnManager = VpnManager.getInstance();
    }

    public static VpnManager getVpnManager() {
        return vpnManager;
    }

    public static VpnUi getVpnUi() {
        return vpnUi;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Vpn vpn = new Vpn();
                vpnUi.createAndShowGui();
            }
        });
    }
}
