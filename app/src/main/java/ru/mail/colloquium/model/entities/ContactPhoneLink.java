package ru.mail.colloquium.model.entities;


import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.ContactPhoneNumber;
import ru.mail.colloquium.toolkit.data.BaseRow;
import ru.mail.colloquium.toolkit.data.DbColumn;
import ru.mail.colloquium.toolkit.data.DbForeignKey;
import ru.mail.colloquium.toolkit.data.DbTable;
import ru.mail.colloquium.toolkit.phonenumbers.PhoneNumberUtils;

@DbTable(name = AppData.TABLE_CONTACTS_PHONES_LINKS)
public class ContactPhoneLink extends BaseRow{
    @DbForeignKey(table = AppData.TABLE_CONTACTS)
    public long contact;

    @DbForeignKey(table = AppData.TABLE_PHONE_NUMBERS)
    public long phone;

    public long abPhoneId;

    public String origin;

    public String searchKey;

    public ContactPhoneLink(Contact contact, ContactPhoneNumber phone) {
        this.contact = contact._id;
        this.phone = phone._id;
        this.abPhoneId = phone.link.abPhoneId;
        this.origin = phone.link.origin;
        this.searchKey = PhoneNumberUtils.digitsOnly(origin);
    }

    public ContactPhoneLink() {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContactPhoneLink{");
        sb.append(_id).append(":");
        sb.append(contact).append("<->").append(phone).append(", ");
        sb.append("origin='").append(origin).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
