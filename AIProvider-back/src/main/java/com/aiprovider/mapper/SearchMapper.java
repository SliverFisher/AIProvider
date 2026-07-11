package com.aiprovider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchMapper {

    @Select("SELECT '${table}' AS _table, '${textCol}' AS _matchField, Id, " +
            "`${textCol}` AS _text, `${subCol}` AS _sub " +
            "FROM `${table}` WHERE `${textCol}` LIKE #{like} LIMIT #{limit}")
    List<Map<String, Object>> searchWithSub(@Param("table") String table,
                                            @Param("textCol") String textCol,
                                            @Param("subCol") String subCol,
                                            @Param("like") String like,
                                            @Param("limit") int limit);

    @Select("SELECT '${table}' AS _table, '${textCol}' AS _matchField, Id, " +
            "`${textCol}` AS _text, NULL AS _sub " +
            "FROM `${table}` WHERE `${textCol}` LIKE #{like} LIMIT #{limit}")
    List<Map<String, Object>> searchWithoutSub(@Param("table") String table,
                                               @Param("textCol") String textCol,
                                               @Param("like") String like,
                                               @Param("limit") int limit);
}