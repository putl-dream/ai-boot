package fun.aiboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 工具表
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tool")
public class Tool implements Serializable {

    /**
     * 工具ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 工具名称
     */
    @TableField("name")
    private String name;

    /**
     * 工具类型
     */
    @TableField("type")
    private String type;

    /**
     * 配置（JSON格式）
     */
    @TableField("config")
    private String config;

    /**
     * 工具描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
