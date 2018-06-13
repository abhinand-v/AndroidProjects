package lgztec.tecdaily;


class CardData {

    private String card_id;
    /*private String card_img_url;*/
    private String card_title;
    private String card_tag;
    //private String card_content_url;
    private String card_time;
    private String fav_state;



    CardData (String card_id, String card_title, String card_tag, String card_time, String fav_state){

        this.setCard_id(card_id);
        /*this.setCard_img_url(card_img_url);*/
        this.setCard_title(card_title);
        this.setCard_tag(card_tag);
        //this.setCard_content_url(card_content_url);
        this.setCard_time(card_time);
        this.setFav_state(fav_state);

    }

    public String getFav_state() {
        return fav_state;
    }

    public void setFav_state(String fav_state) {
        this.fav_state = fav_state;
    }

    public String getCard_id() {
        return card_id;
    }

    private void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    String getCard_title() {
        return card_title;
    }

    private void setCard_title(String card_title) {
        this.card_title = card_title;
    }

    String getCard_tag() {
        return card_tag;
    }

    private void setCard_tag(String card_tag) {
        this.card_tag = card_tag;
    }

    String getCard_time() {
        return card_time;
    }

    private void setCard_time(String card_time) {
        this.card_time = card_time;
    }
}
