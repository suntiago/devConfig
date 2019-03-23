package viroyal.com.dev.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;

/**
 * Created by zy on 2019/1/29.
 */

public class NfcParseUtil {

  public static String dumpTagData(Tag tag) {
    StringBuilder sb = new StringBuilder();
    byte[] id = tag.getId();
    sb.append("ID (hex): ").append(toHex(id)).append('\n');
    sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
    sb.append("ID (dec): ").append(toDec(id)).append('\n');
    sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

    String prefix = "android.nfc.tech.";
    sb.append("Technologies: ");
    for (String tech : tag.getTechList()) {
      sb.append(tech.substring(prefix.length()));
      sb.append(", ");
    }
    sb.delete(sb.length() - 2, sb.length());
    for (String tech : tag.getTechList()) {
      if (tech.equals(MifareClassic.class.getName())) {
        sb.append('\n');
        String type = "Unknown";
        try {
          MifareClassic mifareTag;
          try {
            mifareTag = MifareClassic.get(tag);
          } catch (Exception e) {
            // Fix for Sony Xperia Z3/Z5 phones
            tag = cleanupTag(tag);
            mifareTag = MifareClassic.get(tag);
          }
          switch (mifareTag.getType()) {
            case MifareClassic.TYPE_CLASSIC:
              type = "Classic";
              break;
            case MifareClassic.TYPE_PLUS:
              type = "Plus";
              break;
            case MifareClassic.TYPE_PRO:
              type = "Pro";
              break;
          }
          sb.append("Mifare Classic type: ");
          sb.append(type);
          sb.append('\n');

          sb.append("Mifare size: ");
          sb.append(mifareTag.getSize() + " bytes");
          sb.append('\n');

          sb.append("Mifare sectors: ");
          sb.append(mifareTag.getSectorCount());
          sb.append('\n');

          sb.append("Mifare blocks: ");
          sb.append(mifareTag.getBlockCount());
        } catch (Exception e) {
          sb.append("Mifare classic error: " + e.getMessage());
        }
      }

      if (tech.equals(MifareUltralight.class.getName())) {
        sb.append('\n');
        MifareUltralight mifareUlTag = MifareUltralight.get(tag);
        String type = "Unknown";
        switch (mifareUlTag.getType()) {
          case MifareUltralight.TYPE_ULTRALIGHT:
            type = "Ultralight";
            break;
          case MifareUltralight.TYPE_ULTRALIGHT_C:
            type = "Ultralight C";
            break;
        }
        sb.append("Mifare Ultralight type: ");
        sb.append(type);
      }
    }

    return sb.toString();
  }

  public static Tag cleanupTag(Tag oTag) {
    if (oTag == null)
      return null;

    String[] sTechList = oTag.getTechList();

    Parcel oParcel = Parcel.obtain();
    oTag.writeToParcel(oParcel, 0);
    oParcel.setDataPosition(0);

    int len = oParcel.readInt();
    byte[] id = null;
    if (len >= 0) {
      id = new byte[len];
      oParcel.readByteArray(id);
    }
    int[] oTechList = new int[oParcel.readInt()];
    oParcel.readIntArray(oTechList);
    Bundle[] oTechExtras = oParcel.createTypedArray(Bundle.CREATOR);
    int serviceHandle = oParcel.readInt();
    int isMock = oParcel.readInt();
    IBinder tagService;
    if (isMock == 0) {
      tagService = oParcel.readStrongBinder();
    } else {
      tagService = null;
    }
    oParcel.recycle();

    int nfca_idx = -1;
    int mc_idx = -1;
    short oSak = 0;
    short nSak = 0;

    for (int idx = 0; idx < sTechList.length; idx++) {
      if (sTechList[idx].equals(NfcA.class.getName())) {
        if (nfca_idx == -1) {
          nfca_idx = idx;
          if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
            oSak = oTechExtras[idx].getShort("sak");
            nSak = oSak;
          }
        } else {
          if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
            nSak = (short) (nSak | oTechExtras[idx].getShort("sak"));
          }
        }
      } else if (sTechList[idx].equals(MifareClassic.class.getName())) {
        mc_idx = idx;
      }
    }

    boolean modified = false;

    if (oSak != nSak) {
      oTechExtras[nfca_idx].putShort("sak", nSak);
      modified = true;
    }

    if (nfca_idx != -1 && mc_idx != -1 && oTechExtras[mc_idx] == null) {
      oTechExtras[mc_idx] = oTechExtras[nfca_idx];
      modified = true;
    }

    if (!modified) {
      return oTag;
    }

    Parcel nParcel = Parcel.obtain();
    nParcel.writeInt(id.length);
    nParcel.writeByteArray(id);
    nParcel.writeInt(oTechList.length);
    nParcel.writeIntArray(oTechList);
    nParcel.writeTypedArray(oTechExtras, 0);
    nParcel.writeInt(serviceHandle);
    nParcel.writeInt(isMock);
    if (isMock == 0) {
      nParcel.writeStrongBinder(tagService);
    }
    nParcel.setDataPosition(0);

    Tag nTag = Tag.CREATOR.createFromParcel(nParcel);

    nParcel.recycle();

    return nTag;
  }

  public static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (int i = bytes.length - 1; i >= 0; --i) {
      int b = bytes[i] & 0xff;
      if (b < 0x10)
        sb.append('0');
      sb.append(Integer.toHexString(b));
      if (i > 0) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  public static String toReversedHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; ++i) {
      if (i > 0) {
        sb.append(" ");
      }
      int b = bytes[i] & 0xff;
      if (b < 0x10)
        sb.append('0');
      sb.append(Integer.toHexString(b));
    }
    return sb.toString();
  }

  public static long toDec(byte[] bytes) {
    long result = 0;
    long factor = 1;
    for (int i = 0; i < bytes.length; ++i) {
      long value = bytes[i] & 0xffl;
      result += value * factor;
      factor *= 256l;
    }
    return result;
  }

  public static long toReversedDec(byte[] bytes) {
    long result = 0;
    long factor = 1;
    for (int i = bytes.length - 1; i >= 0; --i) {
      long value = bytes[i] & 0xffl;
      result += value * factor;
      factor *= 256l;
    }
    return result;
  }
}
