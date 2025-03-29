package peersim.custom.baby_cdn;

import peersim.core.Node;

class SimpleReqMessage {

    public String getValue() {
        return value;
    }

    public Node getSender() {
        return sender;
    }

    public Integer getContent() {
        return content;
    }

    final String value;
    final Node sender;
    final Integer content;

    public SimpleReqMessage( String message, int content, Node sender )
    {
        this.value = message;
        this.content = content;
        this.sender = sender;
    }
}
