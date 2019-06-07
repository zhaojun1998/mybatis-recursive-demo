package im.zhaojun.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import im.zhaojun.mapper.MenuMapper;
import im.zhaojun.model.Menu;

import java.util.List;

@Service
public class MenuService{

    @Resource
    private MenuMapper menuMapper;

    public List<Menu> selectMenuTree() {
        return menuMapper.selectMenuTree();
    }
}
