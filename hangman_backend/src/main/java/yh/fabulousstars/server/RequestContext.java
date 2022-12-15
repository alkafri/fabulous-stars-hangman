package yh.fabulousstars.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public record RequestContext(
        String endpoint,
        String session,
        HttpServletRequest req,
        HttpServletResponse resp) {
}
