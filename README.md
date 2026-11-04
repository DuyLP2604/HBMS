# HBMS - Project Structure Guide

## 1. New Web Structure

```text
src/main/webapp/
│
├── assets/
│   └── images/
│
├── css/
│   ├── navbar.css
│   ├── dropdown.css
│   ├── flashMessage.css
│   ├── customer.css
│   ├── employee.css
│   └── ...
│
├── js/
│   ├── flashMessage.js
│   ├── register.js
│   └── ...
│
├── fontawesome/
│
└── WEB-INF/
    ├── components/
    │   ├── navbar.jsp
    │   ├── dropdownMenu.jsp
    │   ├── flashMessage.jsp
    │   └── footer.jsp
    │
    ├── tags/
    │   └── layout.tag
    │
    └── views/
        ├── index.jsp
        ├── login.jsp
        ├── customers.jsp
        ├── employees.jsp
        └── ...
```


## 2. JSP Views

All main JSP pages must be placed inside:

```text
WEB-INF/views/
```

Examples:

- `WEB-INF/views/customers.jsp`
- `WEB-INF/views/employees.jsp`
- `WEB-INF/views/profile.jsp`

Do **NOT** access JSP files directly from the browser.


## 3. Servlet Forward to JSP

Servlets should forward to JSP using:

```java
request.getRequestDispatcher(
    "/WEB-INF/views/yourpagehere.jsp"
).forward(request, response);
```


## 4. Redirect

Always use `contextPath`.

Example:

```java
response.sendRedirect(
    request.getContextPath() + "/customer?action=list"
);
```

Do **NOT** redirect directly to a JSP file.


## 5. Layout

Pages should include:

```jsp
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Customer List"
    pageCss="customer.css"
    useBootstrap="true"
>
    <!-- Page content here -->
</layout:layout>
```

Do **NOT** manually add:

- `<html>`
- `<head>`
- `<body>`
- Navbar
- Bootstrap CSS
- Flash message component
- Common CSS
- Common JavaScript

`layout.tag` already handles them.


## 6. Navbar / Common Components

Common components are located in:

```text
WEB-INF/components/
```

Current common components include:

- `navbar.jsp`
- `dropdownMenu.jsp`
- `flashMessage.jsp`
- `footer.jsp`

Do not copy navbar code into individual JSP pages.

`layout.tag` automatically handles common components and resources.

### Page without navbar

Use:

```jsp
showNavbar="false"
```

Example:

```jsp
<layout:layout
    title="Login"
    pageCss="login.css"
    showNavbar="false"
>
```

### Page using footer

Use:

```jsp
showFooter="true"
```

Example:

```jsp
<layout:layout
    title="Home"
    pageCss="index.css"
    showFooter="true"
>
```


## 7. CSS

### Common CSS

- `css/navbar.css`
- `css/dropdown.css`
- `css/flashMessage.css`

### Page-specific CSS

- `css/customer.css`
- `css/employee.css`
- `css/booking.css`
- `css/complaint.css`
- `css/profile.css`
- ...

Use:

```jsp
pageCss="customer.css"
```

to link page-specific CSS to a page.

Avoid defining generic selectors such as:

- `.navbar`
- `.logo`
- `.dropdown`
- `form`
- `input`
- `button`

inside page-specific CSS when they may affect shared components or other pages.

Prefer specific page classes.

Example JSP:

```jsp
<form
    action="${pageContext.request.contextPath}/register"
    method="post"
    class="register-form"
>
```

Then in CSS:

```css
.register-form {
    /* Register form styles */
}
```

This helps prevent page-specific CSS from overriding common components.


## 8. JavaScript

Page-specific JavaScript should be stored in:

```text
src/main/webapp/js/
```

Examples:

- `js/register.js`
- `js/checkout.js`
- `js/dashboard.js`
- `js/togglePassword.js`

Use:

```jsp
pageJs="register.js"
```

to include JavaScript in a page.

Avoid putting large JavaScript blocks directly inside JSP files because they are harder to read and maintain.


## 9. Static Resources

Use `contextPath` when referencing static resources.

Examples:

```jsp
${pageContext.request.contextPath}/assets/images/...
${pageContext.request.contextPath}/css/...
${pageContext.request.contextPath}/js/...
${pageContext.request.contextPath}/fontawesome/...
```

Static resources must stay **outside `WEB-INF`**.

Examples of static resources:

- CSS files
- JavaScript files
- Images
- Font Awesome files


## 10. Flash Messages

Use Flash inside Servlets.

Available Flash types can be found in `Flash.java`.

Examples:

```java
Flash.success(
    request,
    "Employee added successfully."
);

Flash.error(
    request,
    "Unable to create employee."
);
```

Then redirect:

```java
response.sendRedirect(
    request.getContextPath() + "/employee?action=list"
);
```

Flash messages should be handled by:

```text
Servlet
    ↓
Flash
    ↓
Redirect
    ↓
FlashMessageFilter
    ↓
flashMessage.jsp
```

Do **NOT** use DAO classes to create Flash messages.


## 11. Home Page

`HomeServlet` is mapped to:

```text
/home
```

Do **NOT** map `HomeServlet` to:

```text
/
```

because it may intercept requests for:

- CSS files
- JavaScript files
- Images
- Font Awesome resources

and cause static resources to return the Home page instead.


## 12. Language

UI labels and messages should be written in **English** for consistency.


# How to Create a New Page

1. Create the JSP file:

   ```text
   WEB-INF/views/example.jsp
   ```

2. Create page-specific CSS if needed:

   ```text
   css/example.css
   ```

3. Create page-specific JavaScript if needed:

   ```text
   js/example.js
   ```

4. Use the shared layout:

   ```jsp
   <%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

   <layout:layout
       title="Example"
       pageCss="example.css"
       pageJs="example.js"
       useBootstrap="true"
   >
       <!-- Page content -->
   </layout:layout>
   ```

5. Forward to the JSP from the Servlet:

   ```java
   request.getRequestDispatcher(
       "/WEB-INF/views/example.jsp"
   ).forward(request, response);
   ```

6. Use `contextPath` for links and form actions:

   ```jsp
   <a href="${pageContext.request.contextPath}/customer?action=list">
       Customer List
   </a>
   ```

7. Never open or redirect directly to `example.jsp`.

# Layout Attributes

Use these attributes inside:

```jsp
<layout:layout ...>
```

| Attribute | Use |
|---|---|
| `title` | Page title. Required. |
| `pageCss` | Load CSS for that page. Example: `pageCss="customer.css"` |
| `pageJs` | Load JavaScript for that page. Example: `pageJs="register.js"` |
| `useBootstrap` | Set `true` if the page uses Bootstrap classes |
| `bodyClass` | Add a special class to `<body>` for page-specific styling |
| `showNavbar` | Set `false` to hide navbar |
| `showFooter` | Set `true/false` to control footer |

Example:

```jsp
<layout:layout
    title="Register"
    pageCss="registerCustomer.css"
    pageJs="register.js"
    bodyClass="register-page"
    showNavbar="false"
    showFooter="false"
>
    <!-- Page content -->
</layout:layout>
```

The declarations below belong only in `layout.tag`:

```jsp
<%@ attribute name="title" ... %>
<%@ attribute name="pageCss" ... %>
```

Do not copy them into normal JSP pages.

## Xác nhận thanh toán booking qua PayOS

1. **DB v6 đang dùng:** chạy `db/HBMS_PayOS_Migration.sql` để thêm bảng `PAYOS_ORDER` nếu còn thiếu và cài thủ tục xử lý webhook. **Cài DB mới:** chạy `db/DB_v6_merged_transaction.sql`, bản này đã có bảng và thủ tục PayOS. Script khởi tạo có lệnh xóa DB cũ.
2. Đặt `PAYOS_CLIENT_ID`, `PAYOS_API_KEY` và `PAYOS_CHECKSUM_KEY` trên tiến trình Tomcat. Khi dùng tunnel hoặc reverse proxy, đặt `PAYOS_PUBLIC_BASE_URL` thành URL HTTPS của ứng dụng, gồm cả context path, ví dụ `https://hbms.example.com/HBMS`. Đăng nhập và mở trang thanh toán bằng cùng URL này khi thử.
3. Clean and Build, deploy WAR mới và đăng ký webhook của kênh PayOS tại `<PAYOS_PUBLIC_BASE_URL>/payos-webhook`. Nếu Tomcat chỉ chạy localhost, dùng URL HTTPS của tunnel để PayOS gọi được endpoint.
4. Trên DB test, tạo một booking, chọn PayOS rồi thanh toán. Xác nhận `PAYOS_ORDER.Status = 'PAID'`, có một dòng `PAYMENT` tương ứng và booking đợt đầu lên `CONFIRMED`. Gửi lại cùng webhook để kiểm tra dòng thanh toán vẫn chỉ có một. Webhook thử lúc đăng ký URL được chấp nhận khi có chữ ký hợp lệ, và mã đơn thử không khớp đơn HBMS sẽ không tạo thanh toán.

Nếu PayOS đã báo thành công mà HBMS chưa cập nhật, kiểm tra lịch sử gọi webhook của kênh và log Tomcat. HTTP 401 là chữ ký không khớp. HTTP 503 là backend chưa thể xử lý, chẳng hạn thiếu migration hoặc lỗi kết nối DB. Đơn `FAILED` có `ErrorMessage` để đối chiếu trường hợp sai tiền, quá hạn hoặc số dư ví thay đổi. Trạng thái trên URL quay về chỉ dùng để điều hướng. Thanh toán được ghi sau khi webhook đã xác thực và đối chiếu với đơn lưu trong DB.

- [Webhook PayOS](https://payos.vn/docs/du-lieu-tra-ve/webhook/)
- [Kiểm tra chữ ký](https://payos.vn/docs/tich-hop-webhook/kiem-tra-du-lieu-voi-signature/)

### VietQR tại quầy khi nhận phòng

Sau khi cập nhật code, chạy lại `db/HBMS_PayOS_Migration.sql` trên DB v6 đang dùng để thêm các trường cho QR tại quầy. DB mới đã có các trường này trong script v6. Dùng cùng kênh PayOS, các biến môi trường và webhook `/payos-webhook` đang chạy.

1. Mở check-in của booking đã đặt cọc và đã gán phòng. Chọn **VietQR** rồi bấm **Generate VietQR**.
2. Cho khách quét mã để trả phần tiền phòng còn thiếu. Webhook PayOS cập nhật số tiền đã trả.
3. Kiểm tra CCCD và bấm **Complete check-in**. Phí dịch vụ còn lại thu lúc checkout.

Mã có hạn 15 phút. Tải lại trang sẽ hiện mã đang hoạt động của booking. Nếu khách đã chuyển tiền mà trạng thái cần đối soát hoặc mã hết hạn, kiểm tra giao dịch PayOS trước khi thu lại.
