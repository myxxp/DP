package com.crabdp;

import com.crabdp.service.impl.ShopServicelmpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CrabDPApplicationTests {

    @Autowired
    private ShopServicelmpl shopServicelmpl;


    @Test
    public void testSaveShop() throws InterruptedException {
       shopServicelmpl.saveShop2Redis(1L,10L);
    }
}
