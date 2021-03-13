package org.example.realworldapi.application.utils;

import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.response.ArticleResponse;
import org.example.realworldapi.application.web.model.response.ArticlesResponse;
import org.example.realworldapi.application.web.model.response.CommentResponse;
import org.example.realworldapi.application.web.model.response.ProfileResponse;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.PageResult;
import org.example.realworldapi.domain.model.comment.Comment;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class ResourceUtils {

  private final FindUserByUsername findUserByUsername;
  private final IsFollowingUser isFollowingUser;
  private final FindArticleTags findArticleTags;
  private final IsArticleFavorited isArticleFavorited;
  private final ArticleFavoritesCount articleFavoritesCount;

  public ProfileResponse profileResponse(String username, UUID loggedUserId) {
    final var user = findUserByUsername.handle(username);
    final var profileResponse = new ProfileResponse(user);
    if (loggedUserId != null) {
      profileResponse.setFollowing(isFollowingUser.handle(loggedUserId, user.getId()));
    }
    return profileResponse;
  }

  public ArticleResponse articleResponse(Article article, UUID loggedUserId) {
    final var author = article.getAuthor();
    final var profileResponse = profileResponse(author.getUsername(), author.getId());
    final var tags = findArticleTags.handle(article);
    final var favoritesCount = articleFavoritesCount.handle(article.getId());
    final var articleResponse = new ArticleResponse(article, profileResponse, favoritesCount, tags);
    if (loggedUserId != null) {
      articleResponse.setFavorited(isArticleFavorited.handle(article, loggedUserId));
    }
    return articleResponse;
  }

  public ArticlesResponse articlesResponse(PageResult<Article> pageResult, UUID loggedUserId) {
    final var resultResponse =
        pageResult.getResult().stream()
            .map(article -> articleResponse(article, loggedUserId))
            .collect(Collectors.toList());
    return new ArticlesResponse(resultResponse, pageResult.getTotal());
  }

  public CommentResponse commentResponse(Comment comment, UUID loggedUserId) {
    final var commentAuthor = comment.getAuthor();
    final var authorResponse = profileResponse(commentAuthor.getUsername(), loggedUserId);
    return new CommentResponse(comment, authorResponse);
  }

  public UUID getLoggedUserId(SecurityContext securityContext) {
    Principal principal = securityContext.getUserPrincipal();
    return principal != null ? UUID.fromString(principal.getName()) : null;
  }
}
