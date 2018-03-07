/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.zach.tools.object;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 字符串工具类
 * @author THINK
 *
 */
public class StringUtil {

    //分割字符串正则表达式
    private final static Pattern SPLIT_PATTERN = Pattern.compile("\n|;|,|\\|");

    /**
     * 空字符串
     */
    public final static String EMPTY = "";

    /**
     * 字符串是否是空白字符串
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 是否是true字符串
     * @param str
     * @return
     */
    public static boolean isTrue(String str) {
        return "true".equalsIgnoreCase(str) || "t".equalsIgnoreCase(str) || "1".equals(str) || "yes".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str);
    }

    /**
     * 删除空白字符串
     * @param str
     * @return
     */
    public static String deleteWhitespace(String str) {
        if (str == null) {
            return null;
        }
        int sz = str.length();
        StringBuilder buffer = new StringBuilder(sz);
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                buffer.append(str.charAt(i));
            }
        }
        return buffer.toString();
    }

    /**
     * 连接连个对象数组
     * @param src
     * @param dis
     * @return
     */
    public static Object[] join(Object[] src, Object[] dis) {
        Object[] answer = new Object[src.length + dis.length];
        System.arraycopy(src, 0, answer, 0, src.length);
        System.arraycopy(dis, 0, answer, src.length, dis.length);
        return answer;
    }

    /**
     * 按照\n|;|,|\\|分割字符串,不使用任何注释
     * @param str 字符串
     * @return 分割结果
     */
    public static String[] split(String str) {
        return parseStringSplit(str, (char) 0);
    }

    /**
     * 按照\n|;|,|\\|分割字符串, 首字母是#号表示注释跳过
     * @param str 字符串
     * @return 分割结果
     */
    public static String[] parseStringSplit(String str) {
        return parseStringSplit(str, '#');
    }

    /**
     * 分割字符串按照 \n|;|,|\\|
     * @param str 字符串
     * @param comm 注释字符
     * @return 分割结果
     */
    public static String[] parseStringSplit(String str, char comm) {
        String[] answer = null;
        if (str != null && str.length() > 0) {
            String sp[] = SPLIT_PATTERN.split(str, 0);
            String[] array = new String[sp.length];
            int pos = 0;
            if (sp.length > 0) {
                for (String t : sp) {
                    t = t.trim();
                    if (t.length() == 0 || (comm > 0 && t.charAt(0) == comm)) continue;
                    array[pos++] = t;
                }
                answer = new String[pos];
                System.arraycopy(array, 0, answer, 0, pos);
            } else {
                answer = null;
            }
        }
        return answer;
    }

    /**
     * 占位符标识,支持多种格式占位符:  ${} %{} &{}
     */
    private static final char VARIABLE_TOKEN_OPEN_1_1 = '$';

    private static final char VARIABLE_TOKEN_OPEN_1_2 = '%';

    private static final char VARIABLE_TOKEN_OPEN_1_3 = '&';

    /**
     * 占位符开始
     */
    private static final char VARIABLE_TOKEN_OPEN_2 = '{';

    /**
     * 占位符结束
     */
    private static final char VARIABLE_TOKEN_CLOSE = '}';

    /**
     * 格式化字符串,例如: hello ${word},  ${word}被参数替换
     * @param template 字符串
     * @param values 值
     * @return
     */
    public static String format(String template, Object... values) {
        return parsePropertyTokens(template, null, values);
    }

    /**
     * 格式化字符串,例如: hello ${word},  ${word}被变量替换
     * @param template 字符串
     * @param properties 变量
     * @return
     */
    public static String format(String template, Map<String, Object> properties) {
        return parsePropertyTokens(template, properties, new Object[0]);
    }

    /**
     * 替换系统中的环境变量 ${}
     * @param string 字符串
     * @param properties 配置
     * @return
     */
    public static String parsePropertyTokens(String string, Map<String, Object> properties, Object values[]) {
        String answer = null;
        if (string != null) {
            StringBuilder sb = new StringBuilder();
            char[] chars = string.toCharArray();
            Object propValue = null, propName = null;
            int begin = 0, open = 0, found = 0;
            for (int pos = 0, next = 0, len = chars.length; pos < len; ++pos) {
                next = pos + 1;
                if ((chars[pos] == VARIABLE_TOKEN_OPEN_1_1 || chars[pos] == VARIABLE_TOKEN_OPEN_1_2 || chars[pos] == VARIABLE_TOKEN_OPEN_1_3) && next < len
                    && chars[next] == VARIABLE_TOKEN_OPEN_2) {
                    if (begin >= 0 && pos > 0) {
                        sb.append(string.substring(begin, pos));
                        begin = pos;
                    }
                    open = pos = pos + 2;
                    for (; pos < len && chars[pos] != VARIABLE_TOKEN_CLOSE; pos++) {
                    }
                    if (pos > open) {
                        propName = string.substring(open, pos);
                        if (values != null && propValue == null && found < values.length) {
                            propValue = values[found];
                            found++;
                        } else if (properties != null) {
                            propValue = properties.get(propName);
                        }
                        if (propValue != null) {
                            sb.append(propValue);
                            begin = pos + 1;
                            propValue = null;
                        }
                    }
                }
            }
            if (begin < chars.length) {
                sb.append(string.substring(begin, chars.length));
            }
            answer = sb.toString();
        }
        return answer;
    }

    /**
     * 把Collection对象转换字符串
     * @param collection 列表对象
     * @param sep 分割符号
     * @return
     */
    public static String serialize(Collection<?> collection, String sep) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = collection.iterator();
        boolean bool = false;
        while (it.hasNext()) {
            if (bool) sb.append(sep);
            else bool = true;
            sb.append(it.next());
        }
        return sb.toString();
    }

    /**
     * 列出对象所有属性值
     * @param obj 对象
     * @return
     */
    public static String dump(Object obj) {
        String answer = null;
        if (obj != null) {
            try {
                Class<?> cls = obj.getClass();
                answer = "[[" + obj.getClass().getName() + "[\n";
                Field fieldlist[] = cls.getDeclaredFields();
                for (int i = 0; i < fieldlist.length; i++) {
                    Field fld = fieldlist[i];
                    boolean bool = fld.isAccessible();
                    fld.setAccessible(true);
                    answer += "field [";
                    answer += "name = " + fld.getName() + " ";
                    answer += "value = " + fld.get(obj) + "";
                    //answer += "type = " + fld.getType() + "\n";
                    //answer += "modifiers = " + Modifier.toString(fld.getModifiers()) + "\n";
                    answer += "]\n";
                    fld.setAccessible(bool);
                }
                answer += "]" + "\n";
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return answer;
    }

    /**
     * str==null 或者 str.length ==0 返回true
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Object[]==null 或者 Object[].length ==0 返回true
     * @param objects
     * @return
     */
    public static boolean isEmpty(Object[] objects) {
        return objects == null || objects.length == 0;
    }

    /**
     * obj==null  返回true
     * @param object
     * @return
     */
    public static boolean isEmpty(Object object) {
        return object == null;
    }

    /**
     * collection== null 或者 collection.size()==0 返回true <br />
     * 用于判断 List 类和Set 类
     * @param list
     * @return
     */
    public static boolean isEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * map== null 或者 map.size()==0 返回true
     * @param map
     * @return
     */
    public static boolean isEmpty(Map<?,?> map) {
        return map == null || map.size() == 0;
    }

    /**
     * iterator== null 或者 iterator.size()==0 返回true
     * @param it
     * @return
     */
    public static boolean isEmpty(Iterator<?> it) {
        return it == null || it.hasNext();
    }

    /**
     * enumeration== null 或者 enumeration.size()==0 返回true
     * @param e
     * @return
     */
    public static boolean isEmpty(Enumeration<?> e) {
        return e == null || e.hasMoreElements();
    }

    /**
     *  判定文件夹或者文件是否为空<br />
     *
     * @param file
     * @return
     */
    public static boolean isEmpty(File file) {
        boolean answer = true;
        if (file != null && file.exists()) {
            if (file.isFile()) {
                answer = file.length() == 0;
            } else if (file.isDirectory()) {
                answer = file.listFiles().length == 0;
            }
        }
        return answer;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    public static boolean isNotEmpty(Collection<?> list) {
        return !isEmpty(list);
    }

    public static boolean isNotEmpty(Map<?,?> map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(Iterator<?> it) {
        return !isEmpty(it);
    }

    public static boolean isNotEmpty(Enumeration<?> e) {
        return !isEmpty(e);
    }

    /**
     * 生产length长度的随机字符串
     * @param length
     * @return
     */
    public static String getRandomStr(int length) {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getUUIDString(int length) {
        if (length <= 0) {
            length = 6;
        }
        if (length > 32) {
            length = 32;
        }
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, length);
    }

    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

	public static List<Integer> getDifferenceSetByGuava(List<Integer> big, List<Integer> small) {
		Set<Integer> differenceSet = Sets.difference(Sets.newHashSet(big), Sets.newHashSet(small));
		return Lists.newArrayList(differenceSet);
	}
}
