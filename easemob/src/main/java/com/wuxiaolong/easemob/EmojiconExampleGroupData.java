package com.wuxiaolong.easemob;

import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojicon.Type;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;

import java.util.Arrays;

public class EmojiconExampleGroupData {
    
    private static int[] icons = new int[]{
        R.mipmap.icon_002_cover,  
        R.mipmap.icon_007_cover,  
        R.mipmap.icon_010_cover,  
        R.mipmap.icon_012_cover,  
        R.mipmap.icon_013_cover,  
        R.mipmap.icon_018_cover,  
        R.mipmap.icon_019_cover,  
        R.mipmap.icon_020_cover,  
        R.mipmap.icon_021_cover,  
        R.mipmap.icon_022_cover,  
        R.mipmap.icon_024_cover,  
        R.mipmap.icon_027_cover,  
        R.mipmap.icon_029_cover,  
        R.mipmap.icon_030_cover,  
        R.mipmap.icon_035_cover,  
        R.mipmap.icon_040_cover,  
    };
    
    private static int[] bigIcons = new int[]{
        R.mipmap.icon_002,  
        R.mipmap.icon_007,  
        R.mipmap.icon_010,  
        R.mipmap.icon_012,  
        R.mipmap.icon_013,  
        R.mipmap.icon_018,  
        R.mipmap.icon_019,  
        R.mipmap.icon_020,  
        R.mipmap.icon_021,  
        R.mipmap.icon_022,  
        R.mipmap.icon_024,  
        R.mipmap.icon_027,  
        R.mipmap.icon_029,  
        R.mipmap.icon_030,  
        R.mipmap.icon_035,  
        R.mipmap.icon_040,  
    };
    
    
    private static final EaseEmojiconGroupEntity DATA = createData();
    
    private static EaseEmojiconGroupEntity createData(){
        EaseEmojiconGroupEntity emojiconGroupEntity = new EaseEmojiconGroupEntity();
        EaseEmojicon[] datas = new EaseEmojicon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new EaseEmojicon(icons[i], null, Type.BIG_EXPRESSION);
            datas[i].setBigIcon(bigIcons[i]);
            datas[i].setName("示例"+ (i+1));
            datas[i].setIdentityCode("em"+ (1000+i+1));
        }
        emojiconGroupEntity.setEmojiconList(Arrays.asList(datas));
        emojiconGroupEntity.setIcon(R.drawable.ee_2);
        emojiconGroupEntity.setType(Type.BIG_EXPRESSION);
        return emojiconGroupEntity;
    }
    
    
    public static EaseEmojiconGroupEntity getData(){
        return DATA;
    }
}
