package lgztec.tecdaily;


public class DbCardData {

    //app data
    private static final String SERVER = "http://lgztec.freeiz.com/TecDaily/";
    static final String SERVER_URL = SERVER+"sync.php";
    static final String IMAGE_URL = SERVER+"/Images/";
    static final String PAGE_URL = SERVER+"/Content/";
    static final int DATABASE_VERSION = 1;


    //Card database
    static final String DATABASE_NAME = "techdailydb";
    static final String TABLE_NAME = "cardsdata";
    static final String CARD_ID = "cardid";
    static final String CARD_TITLE = "cardtitle";
    static final String CRAD_TAG = "cardtag";
    static final String CARD_TIME = "cardtime";
    static final String CARD_DATE = "carddate";
    static final String CARD_FAV_STAT = "cardfavstat";

    //user database
    static final String UDB_NAME = "tuserdb";
    public static final String UTABLE_NAME= "userdata";
    public static final String USER_NAME = "username";
    public static final String USER_MAIL = "usermail";
    public static final String USER_IMG = "userimg";
}
