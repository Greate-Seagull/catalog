# FR-CAT-001 — Danh sách yêu cầu phân tích

## 1. Quy tắc nghiệp vụ

| Mã | Quy tắc |
|---|---|
| BR-01 | Ẩn và xóa mềm là hai trạng thái riêng. Ẩn là tạm ngừng bán, bật lại nhanh. Xóa mềm là bỏ khỏi vận hành nhưng vẫn giữ dữ liệu. |
| BR-02 | Không xóa cứng qua API. POS có chế độ offline nên giao dịch có thể được đồng bộ sau khi sản phẩm đã bị xóa mềm; giữ lại bản ghi để vẫn tham chiếu được. |
| BR-03 | `sku` và `barcode` duy nhất trong tenant, chỉ xét các sản phẩm chưa bị xóa mềm. Cho phép dùng lại SKU của sản phẩm đã xóa. |
| BR-04 | `name` được phép trùng; chỉ `sku` và `barcode` là trường duy nhất. |
| BR-05 | `sku` để trống khi tạo thì server tự sinh theo dạng `SP` cộng số thứ tự sáu chữ số (ví dụ `SP000001`). Số thứ tự tăng dần riêng cho từng tenant và không bao giờ được cấp lại, nên các lần tạo đồng thời không bao giờ nhận cùng một mã; số có thể bị hở (ví dụ khi tạo thất bại). Nếu mã vừa sinh đã bị SKU nhập tay chiếm thì bỏ qua và lấy số kế tiếp. Chưa cho tenant tự cấu hình tiền tố ở R1. `barcode` để trống thì giữ trống (giá trị trống không tính là trùng). |
| BR-06 | `costPrice` chỉ được trả về cho người gọi có quyền `catalog.cost.read`; thiếu quyền thì trường bị bỏ khỏi phản hồi, không trả `null`. |
| BR-07 | Gửi hoặc sửa `costPrice` cũng cần quyền `catalog.cost.read`; thiếu quyền thì trả 403 `FORBIDDEN` chỉ rõ trường đó và không ghi gì. |
| BR-08 | Khôi phục đưa sản phẩm về trạng thái `HIDDEN` (không tự bật bán lại); thất bại 409 nếu SKU hoặc mã vạch đã bị sản phẩm khác dùng. |
| BR-09 | SKU là duy nhất trong phạm vi tenant, so sánh không phân biệt hoa thường, và chỉ tính các sản phẩm chưa bị xoá, nên sản phẩm đã xoá không chiếm SKU. |
| BR-10 | Số thứ tự của mã SKU tự sinh không bao giờ được cấp lại kể cả khi sản phẩm mang mã đó đã bị xoá. |
| BR-11 | Khi khôi phục sản phẩm đã xoá, nếu SKU cũ không trùng với bất kỳ sản phầm nào thì giữ nguyên SKU, nếu không thì vẫn khôi phục và gán SKU mới theo dạng <SKU gốc>-duplicate-x, với x là số nguyên dương nhỏ nhất làm cho mã chưa bị sản phẩm chưa xoá nào sử dụng, SKU mới vẫn phải thoả các ràng buộc của SKU (tối đa 64 ký tự, chỉ gồm chữ, số, gạch ngang, gạch dưới) và nếu phần gốc quá dài thì rút gọn phần gốc cho vừa giới hạn, đồng thời kết quả khôi phục phải cho client biết SKU cuối cùng và việc SKU đã bị đổi so với SKU cũ, và việc khôi phục không bao giờ bị từ chối vì lý do trùng SKU. |
| BR-12 | Các dữ liệu liên quan như đơn hàng, tồn kho, phiếu nhập tham chiếu sản phẩm bằng id chứ không dùng SKU làm khoá, nên việc đổi SKU hay dùng lại SKU của sản phẩm đã xoá không làm sai lệch dữ liệu cũ, và tra cứu sản phẩm theo SKU chỉ áp dụng cho sản phẩm chưa xoá. |
| BR-13 | Barcode là duy nhất trong phạm vi tenant, so sánh không phân biệt hoa thường, và chỉ tính các sản phẩm chưa bị xoá. |
| BR-14 | Khi khôi phục mà barcode cũ đã bị sản phẩm chưa xoá sử dụng thì vẫn khôi phục sản phẩm, nhưng barcode không có giá trị, và kết quả khôi phục báo cho client biết barcode cũ đã bị bỏ do trùng (kèm giá trị barcode cũ để người dùng gán lại thủ công nếu muốn). |
| BR-15 | Khi cập nhật, nếu yêu cầu cập nhật hiện tại không thay đổi tài nguyên so với trước đó, cập nhật sẽ không được ghi nhận. Yêu cầu cập nhật không thay đổi tài nguyên là sau khi áp dụng cập nhật, tài nguyên bản mới so với bản cũ không khác nhau, thường do yêu cầu không có giá trị mới (yêu cầu gồm các giá trị cũ, yêu cầu trống). Đổi thứ tự trong danh sách được coi là thay đổi. |

---

## 2. Phân quyền

| Permission | Cho phép |
|---|---|
| `catalog.product.read` | Xem chi tiết, danh sách, tìm kiếm, tra cứu theo mã |
| `catalog.product.write` | Tạo, sửa, ẩn, hiện lại |
| `catalog.product.delete` | Xóa mềm, khôi phục, xem và tìm sản phẩm đã xóa |
| `catalog.cost.read` | Thấy, gửi và sửa `costPrice` |

IAM chưa được thực hiện và sẽ phụ thuộc vào định nghĩa permission ở bảng trên; vai trò và việc gán vai trò cho từng permission do IAM quyết định, nằm ngoài FR này. Service chỉ kiểm tra permission trong token.

---

## 3. Quy định trên chức năng

| Mã | Chức năng | Phương thức và đường dẫn | Quyền | Tham số | Phản hồi thành công | Lỗi chính |
|---|---|---|---|---|---|---|
| FN-01 | Tạo | `POST /products` | `catalog.product.write` | **Header:** `idempotency-key` **Body:** `name` (bắt buộc), `sellingPrice` (bắt buộc), `taxRate` {`type`, `rate`} (bắt buộc), `sku` (tùy chọn, trống thì tự sinh), `barcode`, `categoryId`, `images[]` (tối đa 5), `costPrice` (cần `catalog.cost.read`) | 201 + sản phẩm + header `ETag`; `status` = `ACTIVE`, `version` = 1 | 400, 401/403, 409 (`SKU_DUPLICATE`, `BARCODE_DUPLICATE`), 422 (`CATEGORY_NOT_FOUND`, `IMAGE_NOT_FOUND`) |
| FN-02 | Xem chi tiết | `GET /products/{id}` | `catalog.product.read` | **Path:** `id` (UUID) | 200 + sản phẩm (bỏ `costPrice` nếu thiếu quyền; sản phẩm đã xóa chỉ trả cho người có `catalog.product.delete`) | 401/403, 404 `PRODUCT_NOT_FOUND` |
| FN-03 | Danh sách và tìm kiếm | `GET /products` | `catalog.product.read` | **Query:** `q`, `sku` (khớp chính xác), `barcode` (khớp chính xác), `categoryId`, `status` (mặc định `ACTIVE,HIDDEN`; `DELETED` phải tường minh, chỉ dùng riêng và cần `catalog.product.delete`), `cursor`, `limit` (mặc định 20, tối đa 100) | 200 + {`items`, `nextCursor`}; `nextCursor` = `null` nghĩa là hết dữ liệu | 400 (con trỏ sai, trộn `DELETED` với trạng thái khác), 401/403 |
| FN-04 | Sửa | `PATCH /products/{id}` | `catalog.product.write` | **Path:** `id`. **Header:** `If-Match` = `version` hiện tại (bắt buộc). **Body** (chỉ gửi trường cần đổi): `name`, `sku`, `barcode`, `categoryId`, `images[]` (danh sách mới hoàn chỉnh), `sellingPrice`, `costPrice` (cần `catalog.cost.read`), `taxRate`. Không đổi được `status` | **Code:** 200, **Body:** sản phẩm mới, **Header:** `ETag` = `version` + 1 | 400, 401/403, 404, 409 (trùng mã), 412 `VERSION_MISMATCH`, 422, 428 `PRECONDITION_REQUIRED` |
| FN-05 | Ẩn | `POST /products/{id}/hide` | `catalog.product.write` | **Path:** `id` | 200, `ACTIVE` → `HIDDEN`; idempotent | 404, 409 `INVALID_STATE_TRANSITION` |
| FN-06 | Hiện lại | `POST /products/{id}/unhide` | `catalog.product.write` | **Path:** `id` | 200, `HIDDEN` → `ACTIVE`; idempotent | 404, 409 `INVALID_STATE_TRANSITION` |
| FN-07 | Xóa mềm | `DELETE /products/{id}` | `catalog.product.delete` | **Path:** `id` | 204, ghi `deletedAt`; gọi lặp vẫn 204; cho phép xóa cả khi còn tồn kho | 401/403, 404 |
| FN-08 | Khôi phục | `POST /products/{id}/restore` | `catalog.product.delete` | **Path:** `id` | 200, `DELETED` → `HIDDEN` (không tự bật bán lại); idempotent | 404, 409 (`SKU_DUPLICATE`, `BARCODE_DUPLICATE`, `INVALID_STATE_TRANSITION`) |
| FN-09 | Tra cứu theo mã | `GET /products/lookup` | `catalog.product.read` | **Query:** `sku`, `barcode` (ít nhất một; truyền cả hai thì phải khớp đồng thời; `sku` không phân biệt hoa thường, `barcode` so nguyên văn) | 200 + `{ "id": ... }` | 400 `VALIDATION_FAILED` (không truyền tham số), 404 `PRODUCT_NOT_FOUND` |

### FN-01 Tạo
- Trả 201 kèm sản phẩm và header `ETag`; trạng thái ban đầu là `ACTIVE`, `version` bắt đầu từ 1.
- Phản hồi chỉ gồm các trường người gọi có quyền xem (BR-06).
- `images` chứa các `imageId` do service ảnh cấp; việc tải ảnh lên nằm ngoài FR này.

### FN-02 Xem chi tiết
- Trả 404 nếu sản phẩm không tồn tại hoặc thuộc tenant khác.
- Sản phẩm đã xóa mềm chỉ được trả cho người có `catalog.product.delete`; người khác nhận 404.

### FN-03 Danh sách và tìm kiếm
- Tham số: `q`, `sku` và `barcode` (khớp chính xác), `categoryId`, `status` (mặc định `ACTIVE,HIDDEN`), `cursor`, `limit` (mặc định 20, tối đa 100).
- Ngữ nghĩa của `q`: tìm trên tên, SKU và mã vạch; không phân biệt dấu và hoa thường; mọi từ trong `q` phải khớp. Server tự bỏ dấu và hạ chữ thường cho `q` bằng cùng hàm dùng cho cột `name_search`; client gửi nguyên văn.
- Thứ tự kết quả theo bậc: bậc 1 là SKU hoặc mã vạch khớp chính xác; bậc 2 là SKU hoặc mã vạch khớp tiền tố; bậc 3 là tên bắt đầu bằng `q`; bậc 4 là tên chứa `q`. Trong cùng bậc xếp theo tên, rồi theo `id`. Chưa xếp hạng theo dấu ở R1.
- Không có tham số `sort`: thứ tự cố định, có `q` thì theo bậc, tên, `id`; không có `q` thì theo tên, `id`. Client tự sắp xếp phần đã tải về. Có thể thêm `sort` tùy chọn sau mà không phá API.
- Phân trang theo con trỏ: phản hồi gồm `items` và `nextCursor`; `nextCursor` là `null` nghĩa là hết dữ liệu; không trả tổng số bản ghi. Con trỏ mang theo bậc, tên và `id` của dòng cuối. Tham số phân trang (`cursor`, `limit`) tách khỏi tham số bộ lọc.
- Tìm sản phẩm đã xóa: `status` phải có giá trị `DELETED` tường minh, chỉ dùng được với quyền `catalog.product.delete` (thiếu quyền trả 403), không trộn với trạng thái khác (trả 400). `q` vẫn dùng được trong chế độ này.
- Không cho tìm hay lọc theo `costPrice`.
- Hiệu năng: p95 không quá 200 ms với danh mục tới 50.000 SKU mỗi tenant.

### FN-04 Sửa
- Chỉ gửi trường cần đổi; trường không gửi giữ nguyên.
- Bắt buộc header `If-Match` mang `version` hiện tại; thiếu thì trả 428, lệch thì trả 412.
- Đổi được `name`, `sku`, `barcode`, `categoryId`, `images`, `sellingPrice`, `costPrice`, `taxRate`; không đổi được `status`.
- Đổi `sku` hoặc `barcode` vẫn qua kiểm tra duy nhất, trùng thì trả 409.
- `images` là danh sách mới hoàn chỉnh: không gửi thì giữ nguyên, gửi danh sách rỗng thì xóa hết ảnh, thứ tự mảng là thứ tự hiển thị.
- Đổi `costPrice` cần quyền `catalog.cost.read` (BR-07).

### FN-05, FN-06 Ẩn và hiện lại
- Ẩn đưa `ACTIVE` sang `HIDDEN`; hiện lại đưa `HIDDEN` sang `ACTIVE`. Cả hai idempotent.
- Chuyển trạng thái không hợp lệ (ví dụ ẩn sản phẩm đã xóa) trả 409 `INVALID_STATE_TRANSITION`.

### FN-07 Xóa mềm
- Chuyển sản phẩm sang `DELETED`, ghi `deletedAt`, trả 204; gọi lặp vẫn trả 204.
- Cho phép xóa mềm kể cả khi sản phẩm còn tồn kho.

### FN-08 Khôi phục
- Đưa `DELETED` về `HIDDEN`; trả 409 nếu SKU hoặc mã vạch đã bị sản phẩm khác dùng (BR-08).

### FN-09 Tra cứu theo mã
- Truyền `sku`, `barcode` hoặc cả hai (ít nhất một); không truyền tham số nào trả 400 `VALIDATION_FAILED`.
- Khi truyền cả hai, sản phẩm phải khớp đồng thời cả hai mã.
- Khớp chính xác (`sku` không phân biệt hoa thường, `barcode` so sánh nguyên văn), chỉ xét sản phẩm chưa xóa mềm.
- Chỉ trả `id`: 200 kèm `{ "id": ... }` nếu tìm thấy, hoặc 404 `PRODUCT_NOT_FOUND`. Client cần thông tin sản phẩm thì gọi tiếp `GET /products/{id}`.
- Dùng cho hai việc: quét mã vạch tại POS để xác định sản phẩm, và kiểm tra trùng trên form tạo hoặc sửa. Với kiểm tra trùng, 200 nghĩa là mã đã có sản phẩm dùng, 404 nghĩa là mã còn trống; khi sửa sản phẩm, nếu `id` trả về chính là `id` sản phẩm đang sửa thì không phải trùng. Muốn kiểm tra trùng riêng từng trường thì gọi riêng từng tham số, vì khi truyền cả hai thì chỉ khớp khi cùng một sản phẩm giữ cả hai mã.

---

## 4. Quy định trên trường dữ liệu

### 4.1 Trường dữ liệu và quyền của client

| Trường | Kiểu | Tồn tại | Bắt buộc | Ràng buộc | Duy nhất | Quyền của client | Mục đích và tác dụng |
|---|---|---|---|---|---|---|---|
| `id` | UUID | Luôn có giá trị | Không (server sinh) | Bất biến, không đổi suốt vòng đời sản phẩm | Có (khóa chính) | Chỉ đọc: `catalog.product.read` | Client dùng để chỉ đúng một sản phẩm khi xem chi tiết, sửa, ẩn, hiện lại, xóa mềm, khôi phục và khi các nơi khác tham chiếu sản phẩm. |
| `tenantId` | UUID | Luôn có giá trị | Không (lấy từ token) | Bất biến; client không gửi trường này | Không | Chỉ đọc: `catalog.product.read` | Cho biết sản phẩm thuộc tenant nào; client không cần gửi vì server lấy tenant từ token. |
| `name` | string | Luôn có giá trị | Có | Từ 1 đến 255 ký tự, cắt khoảng trắng đầu và cuối; cho phép tiếng Việt có dấu | Không | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Client dùng để tra cứu và nhận diện sản phẩm trên POS, danh sách và báo cáo. |
| `sku` | string | Luôn có giá trị | Không (tự sinh nếu trống) | Từ 1 đến 64 ký tự, chỉ gồm chữ, số, dấu gạch ngang và gạch dưới; không phân biệt hoa thường; được sửa sau khi tạo | Có, trong tenant | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Mã nội bộ do hộ kinh doanh đặt; client dùng để nhận diện hàng khi nhập kho, kiểm kho và tìm nhanh. |
| `barcode` | string | Có thể không có giá trị | Không | Từ 6 đến 32 ký tự số (hoặc chữ và số với mã nội bộ); mỗi sản phẩm một mã vạch | Có, trong tenant | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Client dùng để quét mã tìm hoặc thêm sản phẩm tại POS và khi nhập kho. |
| `categoryId` | UUID | Có thể không có giá trị | Không | Phải tồn tại, cùng tenant, chưa bị xóa; không có thì thuộc nhóm "Chưa phân loại" | Không | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Client dùng để phân nhóm hàng và lọc danh sách theo danh mục. |
| `images` | danh sách `imageId` | Luôn có giá trị kể cả khi chưa có cũng phải là danh sách rỗng | Không | Tối đa 5 ảnh; không trùng `imageId` trong danh sách; ảnh đầu tiên là ảnh đại diện; `imageId` do service ảnh cấp | Không | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Client dùng để hiển thị hình sản phẩm; ảnh đại diện giúp nhận diện nhanh trên POS và danh sách. |
| `sellingPrice` | số nguyên VND | Luôn có giá trị | Có | Giá bán chưa gồm thuế (thuế tính thêm theo `taxRate`); không âm, tối đa 999.999.999.999; không có phần thập phân | Không | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Client dùng làm giá bán mặc định khi bán hàng tại POS. |
| `costPrice` | số nguyên VND | Có thể không có giá trị | Không | Không âm, tối đa 999.999.999.999; không có phần thập phân | Không | Đọc và ghi đều cần `catalog.cost.read`; thiếu quyền thì không thấy trường này | Client dùng để theo dõi giá vốn phục vụ tính lãi gộp và định giá bán; chỉ người có quyền mới thấy. |
| `taxRate` | object gồm `type` và `rate` | Luôn có giá trị | Có | Thuế suất GTGT của mặt hàng. `type` là `RATE` hoặc `EXEMPT`; nếu là `RATE` thì giá trị `rate` hợp lệ do module thuế suất quy định (module này nằm ngoài FR, chưa thiết kế); các service dùng module đó để thống nhất. `EXEMPT` nghĩa là không chịu thuế, khác với thuế suất 0 phần trăm | Không | Đọc: `catalog.product.read`. Ghi: `catalog.product.write` (khi tạo và sửa) | Client dùng để xác định thuế GTGT áp dụng cho mặt hàng khi tính thuế. |
| `status` | enum | Luôn có giá trị | Không (mặc định `ACTIVE` khi tạo) | `ACTIVE`, `HIDDEN`, `DELETED`; không đổi qua `PATCH` | Không | Đọc: `catalog.product.read`. Đổi qua thao tác riêng: ẩn và hiện lại cần `catalog.product.write`; xóa mềm và khôi phục cần `catalog.product.delete` | Client dùng để biết sản phẩm đang bán, tạm ngừng bán hay đã xóa, và để lọc danh sách. |
| `version` | số nguyên | Luôn có giá trị | Không (server quản lý) | Bắt đầu từ 1, tăng 1 mỗi lần ghi | Không | Đọc: `catalog.product.read`; client gửi lại trong header `If-Match` khi sửa | Client dùng để báo phiên bản đang sửa, tránh ghi đè lên nhau khi hai người cùng sửa một sản phẩm. |
| `createdAt` | timestamp UTC | Luôn có giá trị | Không (server ghi) | Bất biến | Không | Chỉ đọc: `catalog.product.read` | Client dùng để biết sản phẩm được tạo khi nào. |
| `updatedAt` | timestamp UTC | Luôn có giá trị | Không (server ghi) | Cập nhật mỗi lần sản phẩm thay đổi | Không | Chỉ đọc: `catalog.product.read` | Client dùng để biết lần thay đổi gần nhất và nhận ra dữ liệu đã cũ khi đồng bộ. |
| `deletedAt` | timestamp UTC | Có thể không có giá trị | Không (server ghi) | Có giá trị khi `status` là `DELETED` | Không | Chỉ đọc, và chỉ khi có `catalog.product.delete` và đang xem sản phẩm đã xóa | Client dùng để biết sản phẩm bị xóa mềm lúc nào, phục vụ màn hình xem và khôi phục sản phẩm đã xóa. |

### 4.2 Trạng thái sản phẩm

Giá trị: `ACTIVE`, `HIDDEN`, `DELETED`. Chỉ đổi qua các thao tác chuyên biệt, không đổi qua `PATCH`.

| Từ | Sang | Thao tác |
|---|---|---|
| (mới) | `ACTIVE` | Tạo |
| `ACTIVE` | `HIDDEN` | Ẩn |
| `HIDDEN` | `ACTIVE` | Hiện lại |
| `ACTIVE`, `HIDDEN` | `DELETED` | Xóa mềm |
| `DELETED` | `HIDDEN` | Khôi phục |

Hiệu lực của từng trạng thái:

- `ACTIVE`: hiện trên POS, tìm kiếm và bán được.
- `HIDDEN`: không hiện trên POS và không bán được; vẫn thấy ở màn quản lý, nhập kho, báo cáo.
- `DELETED`: không hiện ở mọi danh sách mặc định; vẫn được chứng từ cũ tham chiếu.

---

## 5. Mã lỗi

| HTTP | `code` | Khi nào |
|---|---|---|
| 400 | `VALIDATION_FAILED` | Sai kiểu, thiếu trường bắt buộc, vượt giới hạn, con trỏ không hợp lệ, trộn `DELETED` với trạng thái khác, tra cứu theo mã mà không truyền tham số nào; `details` chỉ rõ trường lỗi |
| 401, 403 | `UNAUTHENTICATED`, `FORBIDDEN` | Thiếu token hoặc thiếu quyền (gồm gửi `costPrice` khi thiếu `catalog.cost.read`, tìm sản phẩm đã xóa khi thiếu `catalog.product.delete`) |
| 404 | `PRODUCT_NOT_FOUND` | Không tồn tại hoặc thuộc tenant khác |
| 409 | `SKU_DUPLICATE`, `BARCODE_DUPLICATE` | Trùng khi tạo, sửa hoặc khôi phục |
| 409 | `INVALID_STATE_TRANSITION` | Chuyển trạng thái không hợp lệ |
| 412 | `VERSION_MISMATCH` | `If-Match` không khớp `version` hiện tại |
| 422 | `CATEGORY_NOT_FOUND`, `IMAGE_NOT_FOUND` | Tham chiếu không hợp lệ |
| 428 | `PRECONDITION_REQUIRED` | Sửa mà thiếu `If-Match` |

---

## 6. Lý do của các quy định

### 6.1 sku

- Độ dài từ 1 đến 64 ký tự: tối thiểu 1 vì `sku` để trống sẽ được server tự sinh (BR-05). Tối đa 64 đủ cho mã nội bộ dạng mô tả (loại hàng, màu, cỡ) mà vẫn ngắn để in lên tem, hiển thị trên màn hình POS nhỏ và giữ chỉ mục gọn. Con số 64 là lựa chọn thiết kế, không phải chuẩn nào, và có thể nới sau.
- Chỉ gồm chữ, số, dấu gạch ngang và gạch dưới: `sku` được gõ bằng bàn phím, quét bằng máy quét, in lên tem, xuất nhập qua Excel hoặc CSV và đưa vào đường dẫn. Ký tự lạ gây lỗi khó nhìn thấy, như khoảng trắng thừa, chữ có dấu viết ở các dạng tổ hợp khác nhau, hoặc ký tự có nghĩa đặc biệt trong tìm kiếm và CSV. Giữ tập ký tự hẹp thì hai mã nhìn giống nhau luôn so sánh đúng và khớp được khi quét.
- Không phân biệt hoa thường: `ao-01` và `AO-01` trông là cùng một mã với người dùng; nếu coi là hai mã thì dễ nhầm hàng khi nhập kho và khi quét. Hệ thống so sánh theo chữ thường và vẫn hiển thị đúng dạng người dùng đã nhập.
- Vì sao không để tự do dù `sku` do hộ kinh doanh quản lý: nội dung mã do hộ kinh doanh quyết định, nhưng server phải bảo đảm `sku` duy nhất, tìm được bằng khớp chính xác và khớp tiền tố, và an toàn khi đi qua các nơi khác. Giới hạn này nhằm chặn lỗi, không nhằm áp đặt cách đặt mã. Nới tập ký tự về sau không làm hỏng dữ liệu đã có, còn siết lại thì có, nên R1 chọn hẹp. Nếu hộ kinh doanh cần thêm ký tự như dấu chấm hoặc dấu gạch chéo thì thêm vào tập cho phép.

### 6.2 barcode

- Độ dài từ 6 đến 32 ký tự: mã vạch thương mại thông dụng (EAN-8, UPC-A, EAN-13, GTIN-14) dài từ 8 đến 14 chữ số. Mức 32 chừa chỗ cho mã nội bộ có tiền tố, mức 6 là tối thiểu cho mã nội bộ ngắn. Hai con số này là lựa chọn thiết kế.
- Chỉ gồm số, hoặc chữ và số với mã nội bộ: mã vạch thương mại luôn là số. Máy quét gửi chuỗi ký tự như bàn phím và hệ thống khớp từng ký tự đúng như in trên tem, nên ký tự lạ làm khớp sai. Mã nội bộ do cửa hàng tự đặt hoặc hệ thống sinh có thể có chữ.
- So sánh nguyên văn, không chuẩn hóa hoa thường: mã vạch thương mại là số nên không có chuyện hoa thường; với mã nội bộ có chữ thì hai mã khác hoa thường được coi là khác nhau.
- Vì sao không để tự do: cùng lý do với `sku`, và mã vạch còn là đầu vào của máy quét nên càng cần chặt.

### 6.3 Danh sách ảnh

- Không trùng `imageId` trong cùng một danh sách: lặp một ảnh hai lần không có ý nghĩa hiển thị, và tốn chỗ trong giới hạn 5 ảnh. Trùng thì trả 400 `VALIDATION_FAILED`.
- Việc dùng chung một ảnh cho nhiều sản phẩm khác nhau chưa được quy định.

### 6.4 Phiên bản khi sửa (`If-Match`)

Server biết phiên bản hiện tại của sản phẩm, nhưng không biết client đang sửa dựa trên phiên bản nào. Header `If-Match` là cách client nói "tôi sửa từ bản này"; nếu bản đã đổi trong lúc client đang mở thì server từ chối thay vì ghi đè lặng lẽ.

Ví dụ:

1. Sản phẩm "Nước suối Aquafina" đang ở `version` 3, giá bán 6.000.
2. Chủ hộ và nhân viên cùng mở màn sửa sản phẩm này; cả hai đều thấy `version` 3 và giá 6.000.
3. Chủ hộ đổi giá thành 7.000 và lưu với `If-Match: 3`. Server ghi và sản phẩm lên `version` 4.
4. Nhân viên, vẫn thấy giá 6.000, đổi giá thành 6.500 và lưu với `If-Match: 3`. Server thấy bản hiện tại đã là `version` 4 nên trả 412; nhân viên phải tải lại, thấy giá 7.000 do chủ hộ vừa đổi, rồi quyết định.
5. Nếu không có `If-Match`, bước 4 sẽ thành công và ghi đè giá 7.000 bằng 6.500 mà không ai hay biết.

Khi client không gửi `If-Match`, server không có cách phân biệt cập nhật mới với cập nhật dựa trên dữ liệu cũ, nên thiếu `If-Match` thì trả 428. Lưu ý hai người sửa hai trường khác nhau của cùng một sản phẩm vẫn gây 412 cho người lưu sau; client chỉ cần tải lại rồi gửi lại.
