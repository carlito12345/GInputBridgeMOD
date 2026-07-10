package com.salat.gbinder.features.clusterBackground.data

// QNX access details and file paths for the instrument cluster
// Same connection the community russification used - Telnet plus password, files bridged through
// the NFS shared folder that QNX sees as /shared and Android sees under /data/vendor/nfs/shared
internal object QnxPaths {
    const val HOST = "192.168.118.2"
    const val USER = "root"
    const val PASSWORD = "fGh4dalvHp4ubmb2"

    // Cluster background archive on the read-only /apps partition
    const val CLUSTER_DIR = "/apps/cluster/FX11_HEV/bin/hmi/FX11_HEV/ClusterHMI"
    const val TARGET = "$CLUSTER_DIR/background.kzb"

    // On-device rollback copy kept next to the target during the write window
    const val TARGET_BAK = "$TARGET.gib.bak"

    // NFS shared bridge - same bytes on both sides
    const val ANDROID_SHARED_DIR = "/data/vendor/nfs/shared/"
    const val ANDROID_SHARED_BG = "${ANDROID_SHARED_DIR}background.kzb"
    const val ANDROID_SHARED_B64 = "${ANDROID_SHARED_DIR}background.kzb.b64"
    const val QNX_SHARED_BG = "/shared/background.kzb"

    // Fallback /apps mount device from the reference firmware, used only if runtime discovery fails
    const val FALLBACK_APPS_DEVICE = "/dev/disk/uda0.713CFB34-9488-44D9-9382-401F21CCCAB3.33"
}
