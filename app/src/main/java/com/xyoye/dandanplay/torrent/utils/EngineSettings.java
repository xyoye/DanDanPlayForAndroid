package com.xyoye.dandanplay.torrent.utils;

public class EngineSettings {
        private static final int MAX_PORT_NUMBER = 65535;
        private static final int MIN_PORT_NUMBER = 49160;

        public int cacheSize = 256;
        public int activeDownloads = 3;
        public int activeSeeds = 3;
        public int maxPeerListSize = 200;
        public int tickInterval = 1000;
        public int inactivityTimeout = 60;
        public int connectionsLimit = 200;
        public int connectionsLimitPerTorrent = 40;
        public int uploadsLimitPerTorrent = 2;
        public int port = EngineSettings.MIN_PORT_NUMBER + (int) (Math.random()
                * ((EngineSettings.MAX_PORT_NUMBER - EngineSettings.MIN_PORT_NUMBER) + 1));

        public int activeLimit = TorrentConfig.getInstance().getMaxTaskCount();
        public int downloadRateLimit = TorrentConfig.getInstance().getMaxDownloadRate();
        public int uploadRateLimit = TorrentConfig.getInstance().getMaxUploadRate();
        public boolean dhtEnabled = TorrentConfig.getInstance().isDhtEnable();
        public boolean lsdEnabled = TorrentConfig.getInstance().isLsdEnable();
        public boolean utpEnabled = TorrentConfig.getInstance().isUtpEnable();
        public boolean upnpEnabled = TorrentConfig.getInstance().isUpnpEnable();
        public boolean natPmpEnabled = TorrentConfig.getInstance().isNatPmpEnable();
        public boolean autoManaged = true;
    }