package com.therainbowville.minegasm.client;

import com.therainbowville.minegasm.config.MinegasmConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metafetish.buttplug.client.ButtplugClientDevice;
import org.metafetish.buttplug.client.ButtplugWSClient;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public class ToyController {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ButtplugWSClient client = new ButtplugWSClient("Minegasm");
    private static ButtplugClientDevice device = null;
    private static boolean shutDownHookAdded = false;
    public static String lastErrorMessage = "";
    public static boolean isConnected = false;
    public static double currentVibrationLevel = 0;

    public static boolean connectDevice() {
        try {
            LOGGER.info("URL: " + MinegasmConfig.buttplug.serverUrl);
            client.Disconnect();
            client.Connect(new URI(MinegasmConfig.buttplug.serverUrl), true);

            device = null;
            client.startScanning();

            Thread.sleep(2000);
            client.requestDeviceList();

            LOGGER.info("Enumerating devices...");

            List<ButtplugClientDevice> devices = client.getDevices();

            int nDevices = devices.size();
            LOGGER.info(nDevices);

            if (nDevices < 1) {
                lastErrorMessage = "No device found";
            }

            for (ButtplugClientDevice dev : devices) {
                if (dev.allowedMessages.contains(SingleMotorVibrateCmd.class.getSimpleName())) {
                    LOGGER.info(dev.name);
                    device = dev;
                    try {
                        client.sendDeviceMessage(device, new SingleMotorVibrateCmd(device.index, 0, client.getNextMsgId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            if (Objects.nonNull(device) && !shutDownHookAdded) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        LOGGER.info("Disconnecting devices...");
                        client.stopAllDevices();
                        client.Disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

                shutDownHookAdded = true;
            }

            if (nDevices > 0) {
                isConnected = true;
            }
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
        }

        return Objects.nonNull(device);
    }

    public static void setVibrationLevel(double level) {
        if (Objects.isNull(device)) return;

        if (MinegasmConfig.vibrate) {
            try {
                client.sendDeviceMessage(device, new SingleMotorVibrateCmd(device.index, level, client.getNextMsgId()));
                currentVibrationLevel = level;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (currentVibrationLevel > 0) {
                try {
                    level = 0;
                    client.sendDeviceMessage(device, new SingleMotorVibrateCmd(device.index, level, client.getNextMsgId()));
                    currentVibrationLevel = level;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getDeviceName() {
        return (Objects.nonNull(device)) ? device.name : "<none>";
    }

    public static long getDeviceId() {
        return (Objects.nonNull(device)) ? device.index : -1;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
