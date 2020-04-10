import java.util.*;
/**
 * class that represents the email
 */
public class eMail {
    private String content;
    private boolean isSpam; // true(C=1) if the mail is spam, false(C=0) if not.

    public eMail(String content, boolean isSpam)
    {
        this.content=content;
        this.isSpam=isSpam;
    }

    public String getContent()
    {
        return this.content;
    }

    public boolean isSpam()
    {
        return this.isSpam; //used for checking if spam also!
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSpam(boolean isSpam) {
        this.isSpam = isSpam;
    }


}
