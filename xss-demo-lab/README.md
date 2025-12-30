以下是基于Vue前端 + Spring Boot后端的XSS演示总结，包括存储型、反射型和DOM型XSS的模拟步骤。这些步骤假设你已经应用了代码修改（前端用v-html或innerHTML渲染用户输入，后端存储恶意数据）。

1. 存储型XSS（Stored XSS）
描述：恶意脚本存储在数据库中，当其他用户查看时执行。
前提：前端Posts.vue中帖子body用<span v-html="p.body"></span>渲染；后端JsonApiController的initData()中添加恶意帖子。

模拟步骤：

启动Spring Boot后端（端口8080），确保数据初始化包含恶意帖子（body: <img src=x onerror="alert('Stored XSS!')">）。
启动Vue前端：cd /path/to/frontend && npm run dev（端口5173）。
登录应用（email: alice@example.com, password: 123456）。
进入Posts页面，查看恶意帖子。
预期结果：页面加载时弹出alert框 "Stored XSS!"（因为v-html执行了<img>的onerror事件）。
安全风险：攻击者创建恶意帖子，所有查看者受害。

2. 反射型XSS（Reflected XSS）
描述：恶意脚本通过URL参数传递，直接在页面上反射执行，不存储。
前提：前端Posts.vue中从URL读取q参数并用<div v-html="reflectedXSS"></div>渲染。

模拟步骤：

启动Spring Boot后端（端口8080）。
启动Vue前端：npm run dev（端口5173）。
在浏览器地址栏手动输入恶意URL：http://localhost:5173/?q=<img src=x onerror="alert('Reflected XSS!')"> 并访问。
页面加载，显示“Search Result”部分。
预期结果：弹出alert框 "Reflected XSS!"（URL参数被v-html渲染执行）。
安全风险：攻击者诱导用户点击恶意链接，脚本在用户浏览器执行。

3. DOM型XSS（DOM-based XSS）
描述：恶意脚本通过客户端JavaScript直接修改DOM执行，不涉及服务器。
前提：前端Posts.vue中添加DOM注入功能，用document.getElementById('dom-demo').innerHTML = domInput.value。

模拟步骤：

启动Spring Boot后端（端口8080）。
启动Vue前端：npm run dev（端口5173）。
登录应用，进入Posts页面。
在“DOM XSS Demo”输入框输入：<img src=x onerror="alert('DOM XSS!')">。
点击“Inject”按钮。
预期结果：<div id="dom-demo">区域弹出alert框 "DOM XSS!"（innerHTML注入了恶意HTML，触发事件）。
安全风险：攻击者利用前端代码漏洞注入脚本，完全在客户端发生。

通用注意事项
触发向量：使用<img src=x onerror="alert('XSS!')">而非<script>，因为浏览器阻止动态<script>执行。
修复建议：生产中用{{ }}转义或DOMPurify库清理输入。
测试环境：确保无CSP（Content Security Policy）阻止脚本。
依赖：Vue 3, Axios, Spring Boot。

## XSS修复说明

为了修复xssDemoLabBackendAfter和xssDemoLabFrontAfter工程中的XSS漏洞，我们采取了以下措施：

### 后端修复 (xssDemoLabBackendAfter)
- 移除了初始化数据中的恶意帖子，将包含<script>标签的body替换为安全的文本内容。
- 这样防止了存储型XSS的演示数据。

### 前端修复 (xssDemoLabFrontAfter)
- **存储型XSS修复**：在Posts.vue中，将帖子body的渲染从`<span v-html="p.body"></span>`改为`<span>{{ p.body }}</span>`，使用Vue的插值语法自动转义HTML字符，防止脚本执行。
- **反射型XSS修复**：将搜索结果的渲染从`<div v-html="reflectedXSS"></div>`改为`<div>{{ reflectedXSS }}</div>`，同样使用插值转义。
- **DOM型XSS修复**：在injectDOM函数中，将`innerHTML`改为`textContent`，只设置文本内容而不解析HTML，防止恶意脚本注入。

这些修复确保用户输入不会被当作HTML执行，从而消除了XSS风险。在生产环境中，还应考虑使用CSP（Content Security Policy）等额外安全措施。

### 额外安全措施

为了进一步增强安全性，可以实施以下措施：

#### Content Security Policy (CSP)
CSP是一种安全标准，用于防止XSS、代码注入等攻击。它通过指定允许加载资源的来源来工作。

- **前端实施（Vue应用）**：
  在`xssDemoLabFrontAfter/index.html`的`<head>`标签中添加：
  ```html
  <meta http-equiv="Content-Security-Policy" content="default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';">
  ```
  这将只允许从同一来源加载脚本、样式等资源。

- **后端实施（Spring Boot）**：
  在`xssDemoLabBackendAfter/src/main/java/com/httpcode/test/WebConfig.java`中添加过滤器或在响应头中设置CSP：
  ```java
  @Configuration
  public class WebConfig implements WebMvcConfigurer {
      @Bean
      public FilterRegistrationBean<CspFilter> cspFilter() {
          FilterRegistrationBean<CspFilter> registrationBean = new FilterRegistrationBean<>();
          registrationBean.setFilter(new CspFilter());
          registrationBean.addUrlPatterns("/*");
          return registrationBean;
      }
  }

  public class CspFilter implements Filter {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
          HttpServletResponse httpResponse = (HttpServletResponse) response;
          httpResponse.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';");
          chain.doFilter(request, response);
      }
  }
  ```
