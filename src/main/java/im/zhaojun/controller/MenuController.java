package im.zhaojun.controller;

import im.zhaojun.model.Menu;
import im.zhaojun.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class MenuController {

    @Resource
    private MenuService menuService;

    @GetMapping("/")
    public List<Menu> tree() {
        return menuService.selectMenuTree();
    }
}
