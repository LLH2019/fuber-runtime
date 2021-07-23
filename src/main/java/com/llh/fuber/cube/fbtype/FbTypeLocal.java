package com.llh.fuber.cube.fbtype;

import com.llh.fuber.runtime.fbtype.FbType;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：LLH
 * @date ：Created in 2021/7/23 23:38
 * @description：功能块本地保存缓存
 */
@Data
public class FbTypeLocal {
    public static final Map<String, FbType> fbTypeLocalMap = new HashMap<>();

    static {
        initFbTypeMap();
    }

    private static void initFbTypeMap() {

    }
}
