package viroyal.com.dev.nfc.record;

import android.app.Activity;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.io.UnsupportedEncodingException;

import viroyal.com.dev.R;
import viroyal.com.dev.nfc.NfcParseUtil;


/**
 * Created by zy on 2019/1/29.
 */

public class DefaultRecord implements ParsedNdefRecord {
  /**
   * ISO/IANA language code
   */
  private final String mLanguageCode;

  private final String mText;
  byte[] id;

  private DefaultRecord(String languageCode, String text, byte[] id) {
    mLanguageCode = Preconditions.checkNotNull(languageCode);
    mText = Preconditions.checkNotNull(text);
    this.id = id;
  }

  @Override
  public View getView(Activity activity, LayoutInflater inflater, ViewGroup parent, int offset) {
    TextView text = (TextView) inflater.inflate(R.layout.tag_text, parent, false);
    text.setText(mText);
    return text;
  }

  public String getText() {
    return mText;
  }

  public String getReversedDec() {
    return NfcParseUtil.toReversedDec(id) + "";
  }

  // TODO: deal with text fields which span multiple NdefRecords
  public static DefaultRecord parse(NdefRecord record) {
    try {
      byte[] payload = record.getPayload();
      String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
      int languageCodeLength = payload[0] & 0077;
      String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
      String text =
          new String(payload, languageCodeLength + 1,
              payload.length - languageCodeLength - 1, textEncoding);
      return new DefaultRecord(languageCode, new String(payload), record.getId());
    } catch (UnsupportedEncodingException e) {
      // should never happen unless we get a malformed tag.
      throw new IllegalArgumentException(e);
    }
  }
}
