# 매퍼 골격

인터페이스 (`<기능>/mapper/<Xxx>Mapper.java`)
```java
@Mapper
public interface <Xxx>Mapper {

    /** <설명>. */
    int select<대상>Count(@Param("lotNo") String lotNo, @Param("reason") HoldReason reason);

    /** <설명>. */
    void insert<대상>(@Param("lotNo") String lotNo, @Param("<값>") String <값>);
}
```

XML (`src/main/resources/mapper/<기능>/<Xxx>Mapper.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.shop.<기능>.mapper.<Xxx>Mapper">

  <select id="select<대상>Count" resultType="int">
    SELECT COUNT(*)
      FROM <테이블> WITH (NOLOCK)
     WHERE lot_no = #{lotNo}
  </select>

  <insert id="insert<대상>">
    INSERT INTO <테이블> (lot_no, <컬럼>)
    VALUES (#{lotNo}, #{<값>})
  </insert>

</mapper>
```
