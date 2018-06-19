package app.laiki.service.ab;

public class SyncDataUnit {
    public long phoneId;
    public long contactId;
    public String mimeType;
    public String prefix;
    public String givenName;
    public String familyName;
    public String middleName;
    public String displayName;
    public String number;
    public String normalizedNumber;
    public String photoUri;
    public String photoThumbUri;
    public String birthday;
    public long contactLastUpdatedTimestamp;
    public String accountType;
    public boolean isMobile;

    @Override
    public String toString() {
        return "SyncDataUnit{" +
                "phoneId=" + phoneId +
                ", contactId=" + contactId +
                ", mimeType='" + mimeType + '\'' +
                ", prefix='" + prefix + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", number='" + number + '\'' +
                ", normalizedNumber='" + normalizedNumber + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", photoThumbUri='" + photoThumbUri + '\'' +
                ", birthday='" + birthday + '\'' +
                ", contactLastUpdatedTimestamp='" + contactLastUpdatedTimestamp + '\'' +
                ", accountType='" + accountType + '\'' +
                ", isMobile=" + isMobile +
                '}';
    }
}
