package protocol.process.subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 订阅树的节点，包含父节点、代表节点的token、子节点列表和该节点包含的客户端ID，此部分参考moquette
 * 
 */
public class TreeNode {
    private TreeNode parent;    // 父节点
	private Token token;        // 代表节点的Token
    private List<TreeNode> children = new CopyOnWriteArrayList<TreeNode>(); // 子节点列表
	private List<Subscription> subscriptions = new ArrayList<Subscription>();   // ClientId列表，每个subscription代表一个clientId
    
    public TreeNode(TreeNode parent) {
    	this.parent = parent;
	}

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
	 * 添加新的clientId，clientId包含在subscription中，要注意避免重复添加和qos不一致但存在的情况
	 */
    void addSubscription(Subscription subscription){
    	// 避免同样的订阅添加进来
    	if (subscriptions.contains(subscription)) {
			return;
		}
    	
    	// 同一节点中topic是一样的，所以判断clientId是否重复，若topic和clientId一样，但qos不一样，就移除，重新添加
    	for (Subscription s : subscriptions) {
    		if (s.getClientId().equals(subscription.getClientId())) {
				return;
			}
    	}
    	
    	subscriptions.add(subscription);
    }
    
	/**
	 * 移除已订阅的主题
	 */
    void removeSubscription(Subscription subscription){
    	// 避免同样的订阅添加进来
    	if (subscriptions.contains(subscription)) {
			return;
		}
    	
    	// 同一节点中topic是一样的，所以判断clientId是否重复，若topic和clientId一样，但qos不一样，就移除，重新添加
    	for (Subscription s : subscriptions) {
    		if (s.getClientId().equals(subscription.getClientId())) {
				return;
			}
    	}
    	
    	subscriptions.add(subscription);
    }
    
	/**
	 * 添加子节点
	 */
    void addChild(TreeNode child){
    	children.add(child);
    }
    
    /**
	 * 查询该节点的子节点是否包含了某个token，包含了就返回节点，不包含则返回null
	 */
    TreeNode childWithToken(Token token) {
        for (TreeNode child : children) {
            if (child.getToken().equals(token)) {
                return child;
            }
        }
        return null;
    }
    
    /**
   	 * 返回此节点下的所有子孙节点
   	 */
    List<TreeNode> getAllDescendant() {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (this.children.size() > 0) {
       	    for (TreeNode t : children) {
       		    treeNodes.addAll(t.getAllDescendant());	
       	    }    
        }
        return treeNodes;
    }

    /**
   	 * 取出主题匹配的订阅
   	 */
    void getSubscription(Queue<Token> tokens, List<Subscription> matchingSubs){
    	Token t = tokens.poll();
    	//如果t为null，正面已经取到最后一个token，这时候就直接取出该节点的客户端列表
    	if (t == null) {
			matchingSubs.addAll(subscriptions);
			return;
		}
    	
    	for (TreeNode n : children) {
    		System.out.println(n.getToken().getName());
    		if (n.getToken().getName().equals(t.getName())) {
				n.getSubscription(new LinkedBlockingDeque<Token>(tokens), matchingSubs);
			}
		}
    }
    
    /**
   	 * 移除该节点以及其所有子节点中包含的此clientId
   	 */
    void removeClientSubscription(String clientId){
    	List<Subscription> subsToRemove = new ArrayList<Subscription>();
    	for (Subscription s : subscriptions) {
			if (s.getClientId().equals(clientId)) {
				subsToRemove.add(s);
			}
		}
    	
    	for (Subscription s : subsToRemove) {
			subscriptions.remove(s);
		}
    	
    	//遍历
    	for (TreeNode child : children) {
			child.removeClientSubscription(clientId);
		}
    }
}
