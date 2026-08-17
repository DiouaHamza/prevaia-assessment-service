package msa.emaia.common.chatBoot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatBootRequest {
    private String supplier_id;
    private String user_message;
}
