package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.Model;
import fun.aiboot.mapper.ModelMapper;
import fun.aiboot.service.ModelService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-31
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    @Override
    public Model getByName(String name) {
        return this.getOne(Wrappers.lambdaQuery(Model.class).eq(Model::getName, name));
    }
}
