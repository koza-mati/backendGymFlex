package com.kozikwlodarski.backendgymflex.event;

import java.util.List;

public record ClassCancelledEvent(String classTitle, List<String> recipientEmails) {
}
