package yh.fabulousstars.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public record RequestContext(
        String endpoint,
        HttpSession session,
        HttpServletRequest req,
        HttpServletResponse resp) {
}
