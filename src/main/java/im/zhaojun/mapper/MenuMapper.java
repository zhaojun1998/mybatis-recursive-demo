package im.zhaojun.mapper;

import im.zhaojun.model.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<Menu> selectMenuTree();

}