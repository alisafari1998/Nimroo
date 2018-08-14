package ir.sahab.nimroo.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.model.Link;
import java.util.ArrayList;

public class LinkArraySerializer {

  private static LinkArraySerializer ourInstance = new LinkArraySerializer();

  public static LinkArraySerializer getInstance() {
    return ourInstance;
  }

  private LinkArraySerializer() {}

  public byte[] serialize(ArrayList<Link> links) {

    LinkArrayProto.LinkArray.Builder linkArrayBuilder = LinkArrayProto.LinkArray.newBuilder();

    LinkArrayProto.Link.Builder linkBuilder = LinkArrayProto.Link.newBuilder();
    for (Link link : links) {
      linkBuilder.clear();
      linkBuilder.setLink(link.getLink());
      linkBuilder.setAnchor(link.getAnchor());
      linkArrayBuilder.addLinks(linkBuilder.build());
    }
    LinkArrayProto.LinkArray protoLinkArray = linkArrayBuilder.build();
    return protoLinkArray.toByteArray();
  }

  public ArrayList<Link> deserialize(byte[] input) throws com.github.os72.protobuf351.InvalidProtocolBufferException {

    LinkArrayProto.LinkArray protoLinkArray = LinkArrayProto.LinkArray.parseFrom(input);
    ArrayList<Link> links = new ArrayList<>();
    for (LinkArrayProto.Link protoLink : protoLinkArray.getLinksList()) {
      Link link = new Link();
      link.setAnchor(protoLink.getAnchor());
      link.setLink(protoLink.getLink());
      links.add(link);
    }
    return links;
  }
}
