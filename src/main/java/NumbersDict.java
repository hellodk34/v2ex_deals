
/**
 * @param * @param null:
 * @author hellodk
 * @description NumbersDict 映射阿拉伯数字到 emoji 数字字典
 * @date 9/15/2021 11:23 AM
 * @return
 */

public enum NumbersDict {

    zero(0, "0️⃣"),
    one(1, "1️⃣"),
    two(2, "2️⃣"),
    three(3, "3️⃣"),
    four(4, "4️⃣"),
    five(5, "5️⃣"),
    six(6, "6️⃣"),
    seven(7, "7️⃣"),
    eight(8, "8️⃣"),
    nine(9, "9️⃣");

    private Integer num;
    private String emoji;

    NumbersDict(Integer num, String emoji) {
        this.num = num;
        this.emoji = emoji;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public static String getSpecificEmoji(Integer num) {
        for (NumbersDict item : NumbersDict.values()) {
            if (num == item.getNum()) {
                return item.getEmoji();
            }
        }
        return "";
    }
}
