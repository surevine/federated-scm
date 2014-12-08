package com.surevine.gateway.scm.scmclient.stash;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StashMergeRequestJSONBean {

    private static Logger logger = Logger.getLogger(StashMergeRequestJSONBean.class);
	private String title;

	private HashMap<String, Object> fromRef;
	private HashMap<String, Object> toRef;
	
	public StashMergeRequestJSONBean() {
		//
	}

	public String getTitle() {
		return title;
	}

	public HashMap<String, Object> getFromRef() {
		return fromRef;
	}

	public HashMap<String, Object> getToRef() {
		return toRef;
	}

	public void setTitle(String title) {
		this.title = title;
		logger.debug("Setting title to "+title);
	}

	public void setFromRef(HashMap<String, Object> fromRef) {
		this.fromRef = fromRef;
		logger.debug("Setting fromRef to "+fromRef.toString());
		
	}

	public void setToRef(HashMap<String, Object> toRef) {
		this.toRef = toRef;
		logger.debug("Setting toRed to "+toRef.toString());
	}

}

//{
//    "title": "Talking Nerdy",
//    "description": "It’s a kludge, but put the tuple from the database in the cache.",
//    "state": "OPEN",
//    "open": true,
//    "closed": false,
//    "fromRef": {
//        "id": "refs/heads/feature-ABC-123",
//        "repository": {
//            "slug": "my-repo",
//            "name": null,
//            "project": {
//                "key": "PRJ"
//            }
//        }
//    },
//    "toRef": {
//        "id": "refs/heads/master",
//        "repository": {
//            "slug": "my-repo",
//            "name": null,
//            "project": {
//                "key": "PRJ"
//            }
//        }
//    },
//    "locked": false,
//    "reviewers": [
//        {
//            "user": {
//                "name": "charlie"
//            }
//        }
//    ]
//}