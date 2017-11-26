package com;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.NoSuchElementException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class SimulatedLogin {
    public static void main(String[] args) throws InterruptedException {

        String url = "https://authem14.alipay.com/login/index.htm";
        final String cookieValue = "cna=G9DrEYx8blUCAX1aMTlxWq5v; UM_distinctid=15d646859bc4c3-0414dbf0e13743-36624308-100200-15d646859bd1a2; isg=AmlpROLhFCXg_yhdw5tqScxleBVvJlJ8FA0B2QteQNCQ0ojkU4RDOO3K4kCf; unicard1.vm=\"K1iSL1mnW5bUr+aKP2Lc7w==\"; mobileSendTime=-1; credibleMobileSendTime=-1; ctuMobileSendTime=-1; riskMobileBankSendTime=-1; riskMobileAccoutSendTime=-1; riskMobileCreditSendTime=-1; riskCredibleMobileSendTime=-1; riskOriginalAccountMobileSendTime=-1; ctoken=HVo7jHv265RT3eOt; LoginForm=alipay_login_auth; alipay=\"K1iSL1mnW5bUr+aKP2Lc7zjRroAKXfsp3jwj15YTJZh6DZ/x\"; CLUB_ALIPAY_COM=2088022712180843; iw.userid=\"K1iSL1mnW5bUr+aKP2Lc7w==\"; ali_apache_tracktmp=\"uid=2088022712180843\"; session.cookieNameId=ALIPAYJSESSIONID; _hvn_login=1; CHAIR_SESS=K6iO619fGWnMOmQO_wFsrBC-Akxkpw3OlvniDkmJz3nz3yaIOVYcvpc8lKL9hMlDjv9_cCCDsQWK2P6EuOcPmPWTgkmro7CnjRNj1MEtqJktS7VtGsDIHk0r0yJXQP3F6baR2eBRZF2UBF5PHTcEYw==; spanner=Wgn2qdXz8Kjf8rKDhs69LM44jxpNoevV4EJoL7C0n0A=; zone=GZ00C; ALIPAYJSESSIONID=RZ24KNwb1JtQ9GqbNPaKO8M3xclSG1authRZ24GZ00; rtk=C39tiv5a2jnr84ZaM3uZcdEY0yUwbC/ncQRLO/1D/wlf6SHVOYf";


        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(8181);
        // get the Selenium proxy object
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        // configure it as a desired capability
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        // 使用chrome
//        System.setProperty("webdriver.chrome.driver","F:\\Git\\Payment-Tips\\paying\\lib\\chromedriver.exe");
//        WebDriver driver = new ChromeDriver();

//         使用FireFox
        System.setProperty("webdriver.firefox.bin","geckodriver");
        // 启动firefox的配置
        FirefoxProfile fp = new FirefoxProfile();
        WebDriver driver = new FirefoxDriver(fp);
//         打开一个干净的firefox
//        WebDriver driver = new FirefoxDriver();

        // 使用PhantomJS
//        System.setProperty("phantomjs.binary.path","phantomjs-2.1.1-linux-i686/bin/phantomjs");
//        WebDriver driver = new PhantomJSDriver();

        proxy.addRequestFilter(new RequestFilter() {
            @Override
            public HttpResponse filterRequest(HttpRequest httpRequest, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
                httpRequest.headers().add("Cookie",cookieValue);
                return null;
            }
        });

        // 加载url
        driver.get(url);
//        // 等待加载完成
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        //在界面找到用户名输入栏
        WebElement elemUsername = driver.findElement(By.name("logonId"));
        SimulatedLogin.wait_input(elemUsername,"13420116914");
        Thread.sleep(1);

        //找到密码输入栏
        WebElement elemPassword = driver.findElement(By.name("password_rsainput"));
        SimulatedLogin.wait_input(elemPassword,"ydx73735273.");
        Thread.sleep(1);

        // 获取页面元素:点击确认按钮
        WebElement elemSubmit = driver.findElement(By.id("J-login-btn"));
        Thread.sleep(2);
        elemSubmit.click();

        // 等待加载完成
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

       // 获取当前地址URL
        String current_url = driver.getCurrentUrl();

        if(current_url.indexOf("checkSecurity")>0){
            System.out.println("请输入手机验证码");
            proxy.stop();
            driver.close();
            driver.quit();
            return;
        }
        // 每秒刷新一次界面，并获取数据
        SimulatedLogin.task(driver);

    }

    /***
     *
     * "在输入用户名和密码的时候不能一次性输入，要模拟人的动作，增大每次输入的间隔时间"
     * @param elem "输入内容所在位置"
     * @param content "输入的内容"
     * @throws InterruptedException
     */
    private static void wait_input(WebElement elem,String content) throws InterruptedException {
        char[] str = content.toCharArray();
        for (int i=0;i<str.length;i++){
            String c = String.valueOf(str[i]);
            elem.sendKeys(c);
            Thread.sleep(1);
        }
    }


    /***
     *
     * 定时任务，每隔一秒刷新一次界面，获取最新的交易记录
     * @param driver "浏览器驱动"
     */
    private static void task(WebDriver driver){
        final WebDriver drivers = driver;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //找到第一条交易记录的位置
                WebElement elemAmount = drivers.findElements(By.className("amount-pay")).get(0);
                // 获取交易金额
                String amountValue = elemAmount.getText();
                System.out.println(amountValue);
                // 刷新界面
                drivers.navigate().refresh();
                // 等待加载完成
                drivers.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

            }
        };
        Timer timer = new Timer();
        long delay = 1000;
        long period = 1000;
        timer.scheduleAtFixedRate(task,delay,period);
    }

    /***
     *
     * 检测元素是否存在
     *
     * @param driver "浏览器驱动"
     * @param elem "要测试的元素"
     * @return "存在则返回yes，不存在则返回false
     */
    private static boolean isElemAppear(WebDriver driver,String elem){
        try{
            driver.findElement(By.className(elem));
            return true;
        }catch (NoSuchElementException e){
            return false;
        }
    }
}
